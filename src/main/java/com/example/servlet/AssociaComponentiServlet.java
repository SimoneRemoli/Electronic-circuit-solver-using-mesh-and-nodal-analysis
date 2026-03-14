package com.example.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/AssociaComponentiServlet")
public class AssociaComponentiServlet extends HttpServlet {

    static final class EquationSystem {
        private final String latexMatrix;
        private final String latexExpandedSystem;
        private final String latexAdditionalRelations;
        private final String latexFullSystem;
        private final List<String> componentSymbols;
        private final List<String> extraUnknowns;
        private final Map<String, Integer> currentSourceOrientationByMesh;
        private final Map<String, Integer> voltageSourceOrientationByMesh;

        private EquationSystem(
                String latexMatrix,
                String latexExpandedSystem,
                String latexAdditionalRelations,
                String latexFullSystem,
                List<String> componentSymbols,
                List<String> extraUnknowns,
                Map<String, Integer> currentSourceOrientationByMesh,
                Map<String, Integer> voltageSourceOrientationByMesh
        ) {
            this.latexMatrix = latexMatrix;
            this.latexExpandedSystem = latexExpandedSystem;
            this.latexAdditionalRelations = latexAdditionalRelations;
            this.latexFullSystem = latexFullSystem;
            this.componentSymbols = componentSymbols;
            this.extraUnknowns = extraUnknowns;
            this.currentSourceOrientationByMesh = currentSourceOrientationByMesh;
            this.voltageSourceOrientationByMesh = voltageSourceOrientationByMesh;
        }
    }

    private static String zTerm(String code) {
        if (code == null || code.isBlank()) return "0";
        switch (code.charAt(0)) {
            case 'R':
                return code;
            case 'L':
                return "j w " + code;
            case 'C':
                return "1/(j w " + code + ")";
            default:
                return "0";
        }
    }

    private static String yTerm(String code) {
        String z = zTerm(code);
        return "0".equals(z) ? "0" : "1/(" + z + ")";
    }

    private static void addTerm(StringBuilder sb, String term, int sign) {
        if (sb == null || term == null || term.isBlank() || "0".equals(term) || sign == 0) return;
        if (sb.length() == 0) {
            if (sign < 0) sb.append("- ");
        } else {
            sb.append(sign > 0 ? " + " : " - ");
        }
        sb.append(term);
    }

    private static String valueOf(StringBuilder sb) {
        return (sb == null || sb.length() == 0) ? "0" : sb.toString();
    }

    static String latexifyTerm(String s) {
        if (s == null || s.isBlank()) return "0";
        s = s.replaceAll("1/\\(j w C(\\d+)\\)", "\\\\frac{1}{j\\\\,\\\\omega C_{$1}}");
        s = s.replaceAll("1/\\(R(\\d+)\\)", "\\\\frac{1}{R_{$1}}");
        s = s.replaceAll("1/\\(j w L(\\d+)\\)", "\\\\frac{1}{j\\\\,\\\\omega L_{$1}}");
        s = s.replaceAll("Vg(\\d+)", "V_{g$1}");
        s = s.replaceAll("Ig(\\d+)", "I_{g$1}");
        s = s.replaceAll("Ix(\\d+)", "I_{x$1}");
        s = s.replaceAll("Vx(\\d+)", "V_{x$1}");
        s = s.replaceAll("R(\\d+)", "R_{$1}");
        s = s.replaceAll("L(\\d+)", "L_{$1}");
        s = s.replaceAll("C(\\d+)", "C_{$1}");
        s = s.replaceAll("V(\\d+)", "V_{$1}");
        s = s.replaceAll("I(\\d+)", "I_{$1}");
        s = s.replaceAll("j w", "j\\\\,\\\\omega");
        s = s.replaceAll("\\s+", " ").trim();
        return s.isEmpty() ? "0" : s;
    }

    static String latexifyUnknown(String s) {
        if (s == null || s.isBlank()) return "0";
        if (s.matches("^Vx\\d+$")) {
            return s.replaceAll("Vx(\\d+)", "V_{x$1}");
        }
        if (s.matches("^Ix\\d+$")) {
            return s.replaceAll("Ix(\\d+)", "I_{x$1}");
        }
        if (s.matches("^[A-Za-z]+\\d+$")) {
            return s.replaceAll("([A-Za-z]+)(\\d+)", "$1_{$2}");
        }
        return latexifyTerm(s);
    }

    private static List<List<StringBuilder>> newMatrix(int rows, int cols) {
        List<List<StringBuilder>> matrix = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<StringBuilder> row = new ArrayList<>();
            for (int j = 0; j < cols; j++) {
                row.add(new StringBuilder());
            }
            matrix.add(row);
        }
        return matrix;
    }

    private static List<StringBuilder> newVector(int size) {
        List<StringBuilder> vector = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            vector.add(new StringBuilder());
        }
        return vector;
    }

    private static void appendValues(List<String> output, String[] values) {
        if (values == null) return;
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                output.add(value.trim());
            }
        }
    }

    private static String[][] readAssociations(HttpServletRequest req, int equationCount) {
        List<List<String>> rows = new ArrayList<>();
        int maxSize = 1;
        for (int i = 0; i < equationCount; i++) {
            List<String> components = new ArrayList<>();
            appendValues(components, req.getParameterValues("mesh" + i + "_R"));
            appendValues(components, req.getParameterValues("mesh" + i + "_L"));
            appendValues(components, req.getParameterValues("mesh" + i + "_C"));
            appendValues(components, req.getParameterValues("mesh" + i + "_I"));
            appendValues(components, req.getParameterValues("mesh" + i + "_V"));
            rows.add(components);
            maxSize = Math.max(maxSize, components.size());
        }

        String[][] associations = new String[equationCount][maxSize];
        for (int i = 0; i < equationCount; i++) {
            List<String> components = rows.get(i);
            for (int j = 0; j < components.size(); j++) {
                associations[i][j] = components.get(j);
            }
        }
        return associations;
    }

    static Map<String, Set<Integer>> buildMembership(String[][] associations) {
        Map<String, Set<Integer>> membership = new LinkedHashMap<>();
        for (int equation = 0; equation < associations.length; equation++) {
            for (String code : associations[equation]) {
                if (code == null || code.isBlank()) continue;
                membership.computeIfAbsent(code, ignored -> new LinkedHashSet<>()).add(equation);
            }
        }
        return membership;
    }

    static final class BranchGroup {
        final String label;
        final Set<Integer> entities = new LinkedHashSet<>();
        final Set<String> passiveComponents = new LinkedHashSet<>();

        private BranchGroup(String label) {
            this.label = label;
        }
    }

    private static String branchLabel(Map<String, String> labels, int entity, String code) {
        return labels.getOrDefault(entity + "|" + code, code);
    }

    private static boolean isParallelLabel(String label) {
        return label != null && label.startsWith("PARALLEL_");
    }

    static Map<String, String> readBranchLabels(HttpServletRequest req, int equationCount) {
        Map<String, String> labels = new LinkedHashMap<>();
        String rawSeriesGroups = req.getParameter("seriesGroupsData");
        if (rawSeriesGroups != null && !rawSeriesGroups.isBlank()) {
            String[] groups = rawSeriesGroups.split("\\|");
            for (int g = 0; g < groups.length; g++) {
                String group = groups[g].trim();
                if (group.isEmpty()) {
                    continue;
                }
                String label = "SERIES_" + (g + 1);
                String[] codes = group.split(",");
                for (int i = 0; i < equationCount; i++) {
                    for (String code : codes) {
                        String trimmed = code.trim();
                        if (!trimmed.isEmpty()) {
                            labels.put(i + "|" + trimmed, label);
                        }
                    }
                }
            }
        }

        String rawParallelGroups = req.getParameter("parallelGroupsData");
        if (rawParallelGroups != null && !rawParallelGroups.isBlank()) {
            String[] groups = rawParallelGroups.split("\\|");
            for (int g = 0; g < groups.length; g++) {
                String group = groups[g].trim();
                if (group.isEmpty()) {
                    continue;
                }
                String label = "PARALLEL_" + (g + 1);
                String[] codes = group.split(",");
                for (int i = 0; i < equationCount; i++) {
                    for (String code : codes) {
                        String trimmed = code.trim();
                        if (!trimmed.isEmpty()) {
                            labels.put(i + "|" + trimmed, label);
                        }
                    }
                }
            }
        }
        return labels;
    }

    static Map<String, BranchGroup> buildPassiveBranches(String[][] associations, Map<String, String> branchLabels) {
        Map<String, BranchGroup> branches = new LinkedHashMap<>();
        for (int entity = 0; entity < associations.length; entity++) {
            for (String code : associations[entity]) {
                if (code == null || code.isBlank()) continue;
                char type = code.charAt(0);
                if (type != 'R' && type != 'L' && type != 'C') continue;
                String label = branchLabel(branchLabels, entity, code);
                BranchGroup group = branches.computeIfAbsent(label, BranchGroup::new);
                group.entities.add(entity);
                group.passiveComponents.add(code);
            }
        }
        return branches;
    }

    static String branchImpedance(BranchGroup branch) {
        if (isParallelLabel(branch.label)) {
            StringBuilder admittanceSum = new StringBuilder();
            for (String code : branch.passiveComponents) {
                addTerm(admittanceSum, yTerm(code), 1);
            }
            return "1/(" + valueOf(admittanceSum) + ")";
        }
        StringBuilder sum = new StringBuilder();
        for (String code : branch.passiveComponents) {
            addTerm(sum, zTerm(code), 1);
        }
        return valueOf(sum);
    }

    static String branchAdmittance(BranchGroup branch) {
        if (isParallelLabel(branch.label)) {
            StringBuilder sum = new StringBuilder();
            for (String code : branch.passiveComponents) {
                addTerm(sum, yTerm(code), 1);
            }
            return valueOf(sum);
        }
        return "1/(" + branchImpedance(branch) + ")";
    }

    private static Map<String, Integer> readCurrentSourceOrientations(HttpServletRequest req, int equationCount) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (int i = 0; i < equationCount; i++) {
            for (int g = 1; g <= AnalysisSessionContext.numeroGeneratoriCorrente; g++) {
                String dir = req.getParameter("mesh" + i + "_I_dir_" + g);
                out.put(i + "|Ig" + g, "discorde".equalsIgnoreCase(dir) ? -1 : 1);
            }
        }
        return out;
    }

    static int currentSourceSign(Map<String, Integer> orientations, int meshIndex, String currentSourceCode) {
        return orientations.getOrDefault(meshIndex + "|" + currentSourceCode, 1);
    }

    private static Map<String, Integer> readNodeCurrentSourceOrientations(HttpServletRequest req, int equationCount) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (int i = 0; i < equationCount; i++) {
            for (int g = 1; g <= AnalysisSessionContext.numeroGeneratoriCorrente; g++) {
                String dir = req.getParameter("mesh" + i + "_I_dir_" + g);
                String code = "Ig" + (AnalysisSessionContext.numeroGeneratoriTensione + g);
                out.put(i + "|" + code, "uscente".equalsIgnoreCase(dir) ? -1 : 1);
            }
        }
        return out;
    }

    static int nodeCurrentSourceSign(Map<String, Integer> orientations, int nodeIndex, String currentSourceCode) {
        return orientations.getOrDefault(nodeIndex + "|" + currentSourceCode, 1);
    }

    private static Map<String, Integer> readMeshVoltageSourceOrientations(HttpServletRequest req, int equationCount) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (int i = 0; i < equationCount; i++) {
            for (int g = 1; g <= AnalysisSessionContext.numeroGeneratoriTensione; g++) {
                String dir = req.getParameter("mesh" + i + "_V_dir_" + g);
                String code = "Vg" + (AnalysisSessionContext.numeroGeneratoriCorrente + g);
                out.put(i + "|" + code, "opposto".equalsIgnoreCase(dir) ? -1 : 1);
            }
        }
        return out;
    }

    static int meshVoltageSourceSign(Map<String, Integer> orientations, int meshIndex, String voltageSourceCode) {
        return orientations.getOrDefault(meshIndex + "|" + voltageSourceCode, 1);
    }

    private static Map<String, Integer> readVoltageSourceOrientations(HttpServletRequest req, int equationCount) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (int i = 0; i < equationCount; i++) {
            for (int g = 1; g <= AnalysisSessionContext.numeroGeneratoriTensione; g++) {
                String dir = req.getParameter("mesh" + i + "_V_dir_" + g);
                String code = "Vg" + g;
                out.put(i + "|" + code, "discorde".equalsIgnoreCase(dir) ? -1 : 1);
            }
        }
        return out;
    }

    static int voltageSourceSign(Map<String, Integer> orientations, int nodeIndex, String voltageSourceCode) {
        return orientations.getOrDefault(nodeIndex + "|" + voltageSourceCode, 1);
    }

    private static String signedUnknown(String unknown, int sign) {
        return sign < 0 ? "- " + unknown : unknown;
    }

    private static List<String> collectComponentSymbols(String[][] associations) {
        Set<String> symbols = new LinkedHashSet<>();
        for (String[] row : associations) {
            for (String code : row) {
                if (code != null && !code.isBlank()) {
                    symbols.add(code);
                }
            }
        }
        return new ArrayList<>(symbols);
    }

    private static String matrixLatex(List<List<StringBuilder>> a, List<String> unknowns, List<StringBuilder> b) {
        StringBuilder out = new StringBuilder();
        out.append("\\[\\begin{bmatrix}");
        for (List<StringBuilder> row : a) {
            for (int j = 0; j < row.size(); j++) {
                out.append(latexifyTerm(valueOf(row.get(j))));
                if (j < row.size() - 1) out.append(" & ");
            }
            out.append(" \\\\ ");
        }
        out.append("\\end{bmatrix}\\,");

        out.append("\\begin{bmatrix}");
        for (String unknown : unknowns) {
            out.append(latexifyUnknown(unknown)).append(" \\\\ ");
        }
        out.append("\\end{bmatrix} = ");

        out.append("\\begin{bmatrix}");
        for (StringBuilder rhs : b) {
            out.append(latexifyTerm(valueOf(rhs))).append(" \\\\ ");
        }
        out.append("\\end{bmatrix}\\]");
        return out.toString();
    }

    private static List<String> expandedEquations(List<List<StringBuilder>> a, List<String> unknowns, List<StringBuilder> b) {
        List<String> equations = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            StringBuilder eq = new StringBuilder();
            boolean first = true;
            for (int j = 0; j < unknowns.size(); j++) {
                String coeff = valueOf(a.get(i).get(j));
                if ("0".equals(coeff)) continue;
                if (!first) eq.append(" + ");
                eq.append("(").append(coeff).append(")").append(unknowns.get(j));
                first = false;
            }
            if (first) eq.append("0");
            eq.append(" = ").append(valueOf(b.get(i)));
            equations.add(eq.toString());
        }
        return equations;
    }

    private static String equationsLatex(List<String> equations) {
        if (equations.isEmpty()) return null;
        StringBuilder out = new StringBuilder("\\[\\begin{aligned}");
        for (String equation : equations) {
            out.append(latexifyTerm(equation)).append(" \\\\ ");
        }
        out.append("\\end{aligned}\\]");
        return out.toString();
    }

    private static String fullSystemLatex(List<String> equations, List<String> additionalRelations) {
        StringBuilder out = new StringBuilder("\\[\\left\\{\\begin{aligned}");
        for (String equation : equations) {
            out.append(latexifyTerm(equation)).append(" \\\\ ");
        }
        for (String relation : additionalRelations) {
            out.append(latexifyTerm(relation)).append(" \\\\ ");
        }
        out.append("\\end{aligned}\\right.\\]");
        return out.toString();
    }

    private static EquationSystem buildMeshSystem(HttpServletRequest req, String[][] associations, List<String> unknowns) {
        int n = unknowns.size();
        List<List<StringBuilder>> a = newMatrix(n, n);
        List<StringBuilder> b = newVector(n);
        Map<String, String> vxByCurrentSource = new LinkedHashMap<>();
        Map<String, Integer> orientations = readCurrentSourceOrientations(req, n);
        Map<String, Integer> voltageOrientations = readMeshVoltageSourceOrientations(req, n);
        Map<String, String> branchLabels = readBranchLabels(req, n);
        Map<String, BranchGroup> passiveBranches = buildPassiveBranches(associations, branchLabels);

        List<Set<String>> componentsByMesh = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Set<String> meshComponents = new LinkedHashSet<>();
            for (String code : associations[i]) {
                if (code != null && !code.isBlank()) {
                    meshComponents.add(code);
                }
            }
            componentsByMesh.add(meshComponents);
        }

        for (BranchGroup branch : passiveBranches.values()) {
            String zBranch = branchImpedance(branch);
            for (Integer entity : branch.entities) {
                addTerm(a.get(entity).get(entity), zBranch, 1);
            }
            if (branch.entities.size() == 2) {
                Integer[] shared = branch.entities.toArray(new Integer[0]);
                addTerm(a.get(shared[0]).get(shared[1]), zBranch, 1);
                addTerm(a.get(shared[1]).get(shared[0]), zBranch, 1);
            }
        }

        for (int i = 0; i < n; i++) {
            for (String code : componentsByMesh.get(i)) {
                char type = code.charAt(0);
                if (type == 'V') {
                    addTerm(b.get(i), code, meshVoltageSourceSign(voltageOrientations, i, code));
                } else if (type == 'I') {
                    String vx = vxByCurrentSource.computeIfAbsent(code, ignored -> "Vx" + (vxByCurrentSource.size() + 1));
                    addTerm(b.get(i), vx, 1);
                }
            }
        }

        Map<String, Set<Integer>> membership = buildMembership(associations);

        List<String> additionalRelations = new ArrayList<>();
        for (Map.Entry<String, Set<Integer>> entry : membership.entrySet()) {
            String code = entry.getKey();
            if (!code.startsWith("Ig")) continue;
            Set<Integer> meshes = entry.getValue();
            List<String> lhsTerms = new ArrayList<>();
            for (Integer meshIndex : meshes) {
                lhsTerms.add(signedUnknown(unknowns.get(meshIndex), currentSourceSign(orientations, meshIndex, code)));
            }
            String lhs = String.join(" + ", lhsTerms).replace("+ -", "- ");
            additionalRelations.add(lhs + " = " + code);
        }

        List<String> equations = expandedEquations(a, unknowns, b);
        return new EquationSystem(
                matrixLatex(a, unknowns, b),
                equationsLatex(equations),
                equationsLatex(additionalRelations),
                fullSystemLatex(equations, additionalRelations),
                collectComponentSymbols(associations),
                new ArrayList<>(vxByCurrentSource.values()),
                orientations,
                voltageOrientations
        );
    }

    private static EquationSystem buildNodeSystem(HttpServletRequest req, String[][] associations, List<String> unknowns) {
        int n = unknowns.size();
        List<List<StringBuilder>> a = newMatrix(n, n);
        List<StringBuilder> b = newVector(n);
        List<String> additionalRelations = new ArrayList<>();
        Map<String, Integer> voltageOrientations = readVoltageSourceOrientations(req, n);
        Map<String, Integer> currentOrientations = readNodeCurrentSourceOrientations(req, n);
        List<String> extraUnknowns = new ArrayList<>();
        Map<String, String> currentByVoltageSource = new LinkedHashMap<>();
        Map<String, String> branchLabels = readBranchLabels(req, n);
        Map<String, BranchGroup> passiveBranches = buildPassiveBranches(associations, branchLabels);

        for (BranchGroup branch : passiveBranches.values()) {
            String admittance = branchAdmittance(branch);
            if (branch.entities.size() == 1) {
                Integer node = branch.entities.iterator().next();
                addTerm(a.get(node).get(node), admittance, 1);
            } else if (branch.entities.size() == 2) {
                Integer[] connected = branch.entities.toArray(new Integer[0]);
                addTerm(a.get(connected[0]).get(connected[0]), admittance, 1);
                addTerm(a.get(connected[1]).get(connected[1]), admittance, 1);
                addTerm(a.get(connected[0]).get(connected[1]), admittance, -1);
                addTerm(a.get(connected[1]).get(connected[0]), admittance, -1);
            }
        }

        for (Map.Entry<String, Set<Integer>> entry : buildMembership(associations).entrySet()) {
            String code = entry.getKey();
            Set<Integer> nodes = entry.getValue();
            char type = code.charAt(0);

            if (type == 'I') {
                if (nodes.size() == 1) {
                    Integer node = nodes.iterator().next();
                    addTerm(b.get(node), code, nodeCurrentSourceSign(currentOrientations, node, code));
                } else if (nodes.size() == 2) {
                    Integer[] connected = nodes.toArray(new Integer[0]);
                    addTerm(b.get(connected[0]), code, nodeCurrentSourceSign(currentOrientations, connected[0], code));
                    addTerm(b.get(connected[1]), code, nodeCurrentSourceSign(currentOrientations, connected[1], code));
                }
            } else if (type == 'V') {
                String ix = currentByVoltageSource.computeIfAbsent(code, ignored -> {
                    String index = code.substring(2);
                    String unknown = "Ix" + index;
                    extraUnknowns.add(unknown);
                    return unknown;
                });
                for (Integer node : nodes) {
                    int sign = voltageSourceSign(voltageOrientations, node, code);
                    addTerm(b.get(node), ix, sign);
                }
                if (nodes.size() == 1) {
                    Integer node = nodes.iterator().next();
                    int sign = voltageSourceSign(voltageOrientations, node, code);
                    additionalRelations.add(signedUnknown(unknowns.get(node), sign) + " = " + code);
                } else if (nodes.size() == 2) {
                    Integer[] connected = nodes.toArray(new Integer[0]);
                    int sign1 = voltageSourceSign(voltageOrientations, connected[0], code);
                    int sign2 = voltageSourceSign(voltageOrientations, connected[1], code);
                    String lhs = signedUnknown(unknowns.get(connected[0]), sign1)
                            + " + " + signedUnknown(unknowns.get(connected[1]), sign2);
                    additionalRelations.add(lhs.replace("+ -", "- ") + " = " + code);
                }
            }
        }

        List<String> equations = expandedEquations(a, unknowns, b);
        return new EquationSystem(
                matrixLatex(a, unknowns, b),
                equationsLatex(equations),
                equationsLatex(additionalRelations),
                fullSystemLatex(equations, additionalRelations),
                collectComponentSymbols(associations),
                extraUnknowns,
                voltageOrientations,
                new LinkedHashMap<>()
        );
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CircuitMethod method = AnalysisSessionContext.method;
        int equationCount = AnalysisSessionContext.equationCount;
        List<String> unknowns = AnalysisSessionContext.variableNames;

        if (method == null || equationCount <= 0 || unknowns == null || unknowns.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Configurazione analisi non valida. Riparti dalla schermata iniziale.");
            return;
        }

        String[][] associations = readAssociations(req, equationCount);
        EquationSystem equationSystem = method == CircuitMethod.NODI
                ? buildNodeSystem(req, associations, unknowns)
                : buildMeshSystem(req, associations, unknowns);
        Map<String, Integer> nodeCurrentOrientations = method == CircuitMethod.NODI
                ? readNodeCurrentSourceOrientations(req, equationCount)
                : new LinkedHashMap<>();

        AnalysisSessionContext.associations = associations;
        AnalysisSessionContext.branchLabelByEntityComponent = readBranchLabels(req, equationCount);
        AnalysisSessionContext.componentSymbols = equationSystem.componentSymbols;
        AnalysisSessionContext.extraUnknowns = equationSystem.extraUnknowns;
        if (method == CircuitMethod.MAGLIE) {
            AnalysisSessionContext.currentSourceOrientationByMesh = equationSystem.currentSourceOrientationByMesh;
            AnalysisSessionContext.voltageSourceOrientationByMesh = equationSystem.voltageSourceOrientationByMesh;
            AnalysisSessionContext.currentSourceOrientationByNode = new LinkedHashMap<>();
            AnalysisSessionContext.voltageSourceOrientationByNode = new LinkedHashMap<>();
        } else {
            AnalysisSessionContext.currentSourceOrientationByNode = nodeCurrentOrientations;
            AnalysisSessionContext.voltageSourceOrientationByNode = equationSystem.currentSourceOrientationByMesh;
            AnalysisSessionContext.currentSourceOrientationByMesh = new LinkedHashMap<>();
            AnalysisSessionContext.voltageSourceOrientationByMesh = new LinkedHashMap<>();
        }
        AnalysisSessionContext.latexMatrix = equationSystem.latexMatrix;
        AnalysisSessionContext.latexExpandedSystem = equationSystem.latexExpandedSystem;
        AnalysisSessionContext.latexAdditionalRelations = equationSystem.latexAdditionalRelations;
        AnalysisSessionContext.latexFullSystem = equationSystem.latexFullSystem;

        req.setAttribute("method", method.name());
        req.setAttribute("referenceNodeName", AnalysisSessionContext.referenceNodeName);
        req.setAttribute("latexSystem", equationSystem.latexMatrix);
        req.setAttribute("latexExpandedSystem", equationSystem.latexExpandedSystem);
        req.setAttribute("latexConstraints", equationSystem.latexAdditionalRelations);
        req.setAttribute("latexFullSystem", equationSystem.latexFullSystem);
        req.setAttribute("componentSymbols", equationSystem.componentSymbols);
        req.getRequestDispatcher("/sistema.jsp").forward(req, resp);
    }
}

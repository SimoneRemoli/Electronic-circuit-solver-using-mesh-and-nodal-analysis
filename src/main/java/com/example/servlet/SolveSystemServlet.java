package com.example.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/SolveSystemServlet")
public class SolveSystemServlet extends HttpServlet {

    private static final class NumericSystem {
        private final ComplexNumber[][] matrix;
        private final ComplexNumber[] rhs;
        private final List<String> unknowns;
        private final ComplexNumber[] presetSolution;

        private NumericSystem(ComplexNumber[][] matrix, ComplexNumber[] rhs, List<String> unknowns, ComplexNumber[] presetSolution) {
            this.matrix = matrix;
            this.rhs = rhs;
            this.unknowns = unknowns;
            this.presetSolution = presetSolution;
        }
    }

    private static final class NodeBaseSystem {
        private final ComplexNumber[][] kclMatrix;
        private final ComplexNumber[] currentRhs;
        private final Map<String, Integer> ixColumnByVoltageSource;
        private final ComplexNumber[][] fullMatrix;
        private final ComplexNumber[] fullRhs;
        private final List<String> unknowns;

        private NodeBaseSystem(
                ComplexNumber[][] kclMatrix,
                ComplexNumber[] currentRhs,
                Map<String, Integer> ixColumnByVoltageSource,
                ComplexNumber[][] fullMatrix,
                ComplexNumber[] fullRhs,
                List<String> unknowns
        ) {
            this.kclMatrix = kclMatrix;
            this.currentRhs = currentRhs;
            this.ixColumnByVoltageSource = ixColumnByVoltageSource;
            this.fullMatrix = fullMatrix;
            this.fullRhs = fullRhs;
            this.unknowns = unknowns;
        }
    }

    private static void setBaseAttributes(HttpServletRequest req) {
        req.setAttribute("method", AnalysisSessionContext.method.name());
        req.setAttribute("referenceNodeName", AnalysisSessionContext.referenceNodeName);
        req.setAttribute("latexSystem", AnalysisSessionContext.latexMatrix);
        req.setAttribute("latexExpandedSystem", AnalysisSessionContext.latexExpandedSystem);
        req.setAttribute("latexConstraints", AnalysisSessionContext.latexAdditionalRelations);
        req.setAttribute("latexFullSystem", AnalysisSessionContext.latexFullSystem);
        req.setAttribute("componentSymbols", AnalysisSessionContext.componentSymbols);
        req.setAttribute("topologyJson", AnalysisSessionContext.topologyJson);
    }

    private static double parseDouble(HttpServletRequest req, String name, double defaultValue) {
        String raw = req.getParameter(name);
        if (raw == null || raw.isBlank()) return defaultValue;
        return parseScalar(raw);
    }

    private static ComplexNumber parseSource(HttpServletRequest req, String code) {
        String raw = req.getParameter("phasor_" + code);
        return parseComplex(raw);
    }

    private static ComplexNumber parseComplex(String raw) {
        if (raw == null || raw.isBlank()) {
            return ComplexNumber.ZERO;
        }
        String s = raw.trim().replace(" ", "").replace(',', '.');
        if (!s.contains("j")) {
            return new ComplexNumber(parseScalar(s), 0.0);
        }
        if ("j".equals(s) || "+j".equals(s)) {
            return new ComplexNumber(0.0, 1.0);
        }
        if ("-j".equals(s)) {
            return new ComplexNumber(0.0, -1.0);
        }
        String withoutJ = s.substring(0, s.length() - 1);
        int split = -1;
        for (int i = 1; i < withoutJ.length(); i++) {
            char ch = withoutJ.charAt(i);
            if (ch == '+' || ch == '-') {
                split = i;
            }
        }
        if (split == -1) {
            return new ComplexNumber(0.0, parseImaginary(withoutJ));
        }
        double re = parseScalar(withoutJ.substring(0, split));
        double im = parseImaginary(withoutJ.substring(split));
        return new ComplexNumber(re, im);
    }

    private static double parseScalar(String raw) {
        String value = raw == null ? "" : raw.trim().replace(" ", "").replace(',', '.');
        if (value.isBlank()) {
            return 0.0;
        }
        int slash = value.indexOf('/');
        if (slash > 0 && slash == value.lastIndexOf('/')) {
            double numerator = Double.parseDouble(value.substring(0, slash));
            double denominator = Double.parseDouble(value.substring(slash + 1));
            return numerator / denominator;
        }
        return Double.parseDouble(value);
    }

    private static double parseImaginary(String raw) {
        if (raw == null || raw.isBlank() || "+".equals(raw)) {
            return 1.0;
        }
        if ("-".equals(raw)) {
            return -1.0;
        }
        return parseScalar(raw);
    }

    private static ComplexNumber passiveValue(HttpServletRequest req, String code, double omega) {
        double value = parseDouble(req, "val_" + code, 0.0);
        switch (code.charAt(0)) {
            case 'R':
                return new ComplexNumber(value, 0.0);
            case 'L':
                return new ComplexNumber(0.0, omega * value);
            case 'C':
                if (omega == 0.0 || value == 0.0) {
                    return ComplexNumber.ZERO;
                }
                return new ComplexNumber(0.0, -1.0 / (omega * value));
            default:
                return ComplexNumber.ZERO;
        }
    }

    private static List<Set<String>> componentsByEquation(String[][] associations, int count) {
        List<Set<String>> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Set<String> row = new LinkedHashSet<>();
            for (String code : associations[i]) {
                if (code != null && !code.isBlank()) {
                    row.add(code);
                }
            }
            out.add(row);
        }
        return out;
    }

    private static boolean isParallelBranch(AssociaComponentiServlet.BranchGroup branch) {
        return branch.label != null && branch.label.startsWith("PARALLEL_");
    }

    private static ComplexNumber equivalentImpedance(HttpServletRequest req, AssociaComponentiServlet.BranchGroup branch, double omega) {
        if (isParallelBranch(branch)) {
            ComplexNumber admittance = ComplexNumber.ZERO;
            for (String code : branch.passiveComponents) {
                ComplexNumber impedance = passiveValue(req, code, omega);
                if (!impedance.isZero()) {
                    admittance = admittance.add(new ComplexNumber(1.0, 0.0).divide(impedance));
                }
            }
            return admittance.isZero() ? ComplexNumber.ZERO : new ComplexNumber(1.0, 0.0).divide(admittance);
        }

        ComplexNumber impedance = ComplexNumber.ZERO;
        for (String code : branch.passiveComponents) {
            impedance = impedance.add(passiveValue(req, code, omega));
        }
        return impedance;
    }

    private static ComplexNumber equivalentAdmittance(HttpServletRequest req, AssociaComponentiServlet.BranchGroup branch, double omega) {
        if (isParallelBranch(branch)) {
            ComplexNumber admittance = ComplexNumber.ZERO;
            for (String code : branch.passiveComponents) {
                ComplexNumber impedance = passiveValue(req, code, omega);
                if (!impedance.isZero()) {
                    admittance = admittance.add(new ComplexNumber(1.0, 0.0).divide(impedance));
                }
            }
            return admittance;
        }

        ComplexNumber impedance = equivalentImpedance(req, branch, omega);
        return impedance.isZero() ? ComplexNumber.ZERO : new ComplexNumber(1.0, 0.0).divide(impedance);
    }

    private static Map<String, Integer> topologyBranchOrientationByMesh() {
        Map<String, Integer> out = new LinkedHashMap<>();
        CircuitTopology.Model model = CircuitTopology.parse(
                AnalysisSessionContext.topologyNodesData,
                AnalysisSessionContext.topologyBranchesData
        );
        for (int meshIndex = 0; meshIndex < AnalysisSessionContext.variableNames.size(); meshIndex++) {
            String meshCode = AnalysisSessionContext.variableNames.get(meshIndex);
            for (CircuitTopology.Branch branch : model.branches) {
                int sign = branch.meshCurrents.getOrDefault(meshCode, 0);
                if (sign != 0) {
                    out.put(meshIndex + "|" + branch.id, sign);
                }
            }
        }
        return out;
    }

    private static String pairKey(int firstMesh, int secondMesh) {
        return Math.min(firstMesh, secondMesh) + "|" + Math.max(firstMesh, secondMesh);
    }

    private static Map<String, Integer> sharedBranchCountByPair(Map<String, AssociaComponentiServlet.BranchGroup> passiveBranches) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (AssociaComponentiServlet.BranchGroup branch : passiveBranches.values()) {
            Integer[] entities = branch.entities.toArray(new Integer[0]);
            for (int left = 0; left < entities.length; left++) {
                for (int right = left + 1; right < entities.length; right++) {
                    String key = pairKey(entities[left], entities[right]);
                    counts.put(key, counts.getOrDefault(key, 0) + 1);
                }
            }
        }
        return counts;
    }

    private static int meshCouplingSign(
            int firstMesh,
            int secondMesh,
            AssociaComponentiServlet.BranchGroup branch,
            Map<String, Integer> sharedBranchCounts,
            Map<String, Integer> branchOrientationByMesh
    ) {
        Integer firstOrientation = branchOrientationByMesh.get(firstMesh + "|" + branch.label);
        Integer secondOrientation = branchOrientationByMesh.get(secondMesh + "|" + branch.label);
        if (firstOrientation != null && secondOrientation != null) {
            return firstOrientation == secondOrientation ? 1 : -1;
        }
        if (branch.entities.size() > 2) {
            return sharedBranchCounts.getOrDefault(pairKey(firstMesh, secondMesh), 0) > 1 ? 1 : -1;
        }
        List<String> directions = AnalysisSessionContext.meshDirections;
        String first = directions != null && firstMesh < directions.size() ? directions.get(firstMesh) : "CW";
        String second = directions != null && secondMesh < directions.size() ? directions.get(secondMesh) : "CW";
        return first.equalsIgnoreCase(second) ? -1 : 1;
    }

    private static NumericSystem meshSystem(HttpServletRequest req) {
        int n = AnalysisSessionContext.variableNames.size();
        List<String> extraUnknowns = AnalysisSessionContext.extraUnknowns;
        List<String> unknowns = new ArrayList<>(AnalysisSessionContext.variableNames);
        unknowns.addAll(extraUnknowns);

        int eqCount = n + extraUnknowns.size();
        ComplexNumber[][] matrix = new ComplexNumber[eqCount][unknowns.size()];
        ComplexNumber[] rhs = new ComplexNumber[eqCount];
        for (int i = 0; i < eqCount; i++) {
            rhs[i] = ComplexNumber.ZERO;
            for (int j = 0; j < unknowns.size(); j++) {
                matrix[i][j] = ComplexNumber.ZERO;
            }
        }

        double omega = parseDouble(req, "omega", 1.0);
        Map<String, Set<Integer>> membership = AssociaComponentiServlet.buildMembership(AnalysisSessionContext.associations);
        List<Set<String>> componentsByMesh = componentsByEquation(AnalysisSessionContext.associations, n);
        Map<String, AssociaComponentiServlet.BranchGroup> passiveBranches =
                AssociaComponentiServlet.buildPassiveBranches(
                        AnalysisSessionContext.associations,
                        AnalysisSessionContext.branchLabelByEntityComponent
                );
        Map<String, Integer> sharedBranchCounts = sharedBranchCountByPair(passiveBranches);
        Map<String, Integer> branchOrientationByMesh = topologyBranchOrientationByMesh();
        Map<String, Integer> vxIndex = new LinkedHashMap<>();
        for (int i = 0; i < extraUnknowns.size(); i++) {
            vxIndex.put(extraUnknowns.get(i), n + i);
        }
        Map<String, String> currentToVx = new LinkedHashMap<>();
        int vxCounter = 0;
        for (String code : AnalysisSessionContext.componentSymbols) {
            if (code.startsWith("Ig")) {
                currentToVx.put(code, extraUnknowns.get(vxCounter++));
            }
        }

        for (AssociaComponentiServlet.BranchGroup branch : passiveBranches.values()) {
            ComplexNumber zBranch = equivalentImpedance(req, branch, omega);
            for (Integer entity : branch.entities) {
                matrix[entity][entity] = matrix[entity][entity].add(zBranch);
            }
            Integer[] shared = branch.entities.toArray(new Integer[0]);
            for (int left = 0; left < shared.length; left++) {
                for (int right = left + 1; right < shared.length; right++) {
                    int sign = meshCouplingSign(shared[left], shared[right], branch, sharedBranchCounts, branchOrientationByMesh);
                    matrix[shared[left]][shared[right]] = matrix[shared[left]][shared[right]].add(zBranch.scale(sign));
                    matrix[shared[right]][shared[left]] = matrix[shared[right]][shared[left]].add(zBranch.scale(sign));
                }
            }
        }

        for (int i = 0; i < n; i++) {
            ComplexNumber voltageRhs = ComplexNumber.ZERO;
            for (String code : componentsByMesh.get(i)) {
                char type = code.charAt(0);
                if (type == 'V') {
                    int sign = AssociaComponentiServlet.meshVoltageSourceSign(
                            AnalysisSessionContext.voltageSourceOrientationByMesh,
                            i,
                            code
                    );
                    voltageRhs = voltageRhs.add(parseSource(req, code).scale(sign));
                } else if (type == 'I') {
                    String vx = currentToVx.get(code);
                    Integer column = vxIndex.get(vx);
                    int sign = AssociaComponentiServlet.currentSourceSign(
                            AnalysisSessionContext.currentSourceOrientationByMesh,
                            i,
                            code
                    );
                    matrix[i][column] = matrix[i][column].add(new ComplexNumber(-sign, 0.0));
                }
            }
            rhs[i] = voltageRhs;
        }

        int row = n;
        for (Map.Entry<String, Set<Integer>> entry : membership.entrySet()) {
            String code = entry.getKey();
            if (!code.startsWith("Ig")) continue;
            for (Integer meshIndex : entry.getValue()) {
                int sign = AssociaComponentiServlet.currentSourceSign(
                        AnalysisSessionContext.currentSourceOrientationByMesh,
                        meshIndex,
                        code
                );
                matrix[row][meshIndex] = matrix[row][meshIndex].add(new ComplexNumber(sign, 0.0));
            }
            rhs[row] = parseSource(req, code);
            row++;
        }

        return new NumericSystem(matrix, rhs, unknowns, null);
    }

    private static NodeBaseSystem buildNodeBaseSystem(HttpServletRequest req) {
        int n = AnalysisSessionContext.variableNames.size();
        List<String> unknowns = new ArrayList<>(AnalysisSessionContext.variableNames);
        unknowns.addAll(AnalysisSessionContext.extraUnknowns);
        int extra = AnalysisSessionContext.extraUnknowns.size();
        int size = n + extra;
        ComplexNumber[][] matrix = new ComplexNumber[size][size];
        ComplexNumber[] rhs = new ComplexNumber[size];
        for (int i = 0; i < size; i++) {
            rhs[i] = ComplexNumber.ZERO;
            for (int j = 0; j < size; j++) {
                matrix[i][j] = ComplexNumber.ZERO;
            }
        }
        ComplexNumber[][] kclMatrix = new ComplexNumber[n][n];
        ComplexNumber[] currentRhs = new ComplexNumber[n];
        for (int i = 0; i < n; i++) {
            currentRhs[i] = ComplexNumber.ZERO;
            for (int j = 0; j < n; j++) {
                kclMatrix[i][j] = ComplexNumber.ZERO;
            }
        }

        double omega = parseDouble(req, "omega", 1.0);
        Map<String, Set<Integer>> membership = AssociaComponentiServlet.buildMembership(AnalysisSessionContext.associations);
        Map<String, AssociaComponentiServlet.BranchGroup> passiveBranches =
                AssociaComponentiServlet.buildPassiveBranches(
                        AnalysisSessionContext.associations,
                        AnalysisSessionContext.branchLabelByEntityComponent
                );
        Map<String, Integer> ixColumnByVoltageSource = new LinkedHashMap<>();
        for (String extraUnknown : AnalysisSessionContext.extraUnknowns) {
            if (extraUnknown.startsWith("Ix")) {
                String index = extraUnknown.substring(2);
                ixColumnByVoltageSource.put("Vg" + index, unknowns.indexOf(extraUnknown));
            }
        }

        for (AssociaComponentiServlet.BranchGroup branch : passiveBranches.values()) {
            ComplexNumber y = equivalentAdmittance(req, branch, omega);
            if (branch.entities.size() == 1) {
                Integer node = branch.entities.iterator().next();
                kclMatrix[node][node] = kclMatrix[node][node].add(y);
                matrix[node][node] = matrix[node][node].add(y);
            } else if (branch.entities.size() == 2) {
                Integer[] connected = branch.entities.toArray(new Integer[0]);
                kclMatrix[connected[0]][connected[0]] = kclMatrix[connected[0]][connected[0]].add(y);
                kclMatrix[connected[1]][connected[1]] = kclMatrix[connected[1]][connected[1]].add(y);
                kclMatrix[connected[0]][connected[1]] = kclMatrix[connected[0]][connected[1]].subtract(y);
                kclMatrix[connected[1]][connected[0]] = kclMatrix[connected[1]][connected[0]].subtract(y);
                matrix[connected[0]][connected[0]] = matrix[connected[0]][connected[0]].add(y);
                matrix[connected[1]][connected[1]] = matrix[connected[1]][connected[1]].add(y);
                matrix[connected[0]][connected[1]] = matrix[connected[0]][connected[1]].subtract(y);
                matrix[connected[1]][connected[0]] = matrix[connected[1]][connected[0]].subtract(y);
            }
        }

        for (Map.Entry<String, Set<Integer>> entry : membership.entrySet()) {
            String code = entry.getKey();
            Set<Integer> nodes = entry.getValue();
            char type = code.charAt(0);
            if (type == 'I') {
                ComplexNumber source = parseSource(req, code);
                if (nodes.size() == 1) {
                    Integer node = nodes.iterator().next();
                    int sign = AssociaComponentiServlet.nodeCurrentSourceSign(
                            AnalysisSessionContext.currentSourceOrientationByNode,
                            node,
                            code
                    );
                    currentRhs[node] = currentRhs[node].add(source.scale(sign));
                    rhs[node] = rhs[node].add(source.scale(sign));
                } else if (nodes.size() == 2) {
                    Integer[] connected = nodes.toArray(new Integer[0]);
                    int sign0 = AssociaComponentiServlet.nodeCurrentSourceSign(
                            AnalysisSessionContext.currentSourceOrientationByNode,
                            connected[0],
                            code
                    );
                    int sign1 = AssociaComponentiServlet.nodeCurrentSourceSign(
                            AnalysisSessionContext.currentSourceOrientationByNode,
                            connected[1],
                            code
                    );
                    currentRhs[connected[0]] = currentRhs[connected[0]].add(source.scale(sign0));
                    currentRhs[connected[1]] = currentRhs[connected[1]].add(source.scale(sign1));
                    rhs[connected[0]] = rhs[connected[0]].add(source.scale(sign0));
                    rhs[connected[1]] = rhs[connected[1]].add(source.scale(sign1));
                }
            } else if (type == 'V') {
                Integer ixColumn = ixColumnByVoltageSource.get(code);
                if (ixColumn != null) {
                    for (Integer node : nodes) {
                        int sign = AssociaComponentiServlet.voltageSourceSign(
                                AnalysisSessionContext.voltageSourceOrientationByNode,
                                node,
                                code
                        );
                        matrix[node][ixColumn] = matrix[node][ixColumn].subtract(new ComplexNumber(sign, 0.0));
                    }
                }
            }
        }

        int extraRow = n;
        for (Map.Entry<String, Set<Integer>> entry : membership.entrySet()) {
            String code = entry.getKey();
            Set<Integer> nodes = entry.getValue();
            if (!code.startsWith("Vg") || nodes.isEmpty()) {
                continue;
            }

            int targetRow = extraRow++;
            for (int j = 0; j < size; j++) {
                matrix[targetRow][j] = ComplexNumber.ZERO;
            }
            rhs[targetRow] = parseSource(req, code);

            for (Integer node : nodes) {
                int sign = AssociaComponentiServlet.voltageSourceSign(
                        AnalysisSessionContext.voltageSourceOrientationByNode,
                        node,
                        code
                );
                matrix[targetRow][node] = matrix[targetRow][node].add(new ComplexNumber(sign, 0.0));
            }
        }
        return new NodeBaseSystem(kclMatrix, currentRhs, ixColumnByVoltageSource, matrix, rhs, unknowns);
    }

    private static ComplexNumber[] computeNodeCurrents(NodeBaseSystem base, ComplexNumber[] eSolution) {
        int n = AnalysisSessionContext.variableNames.size();
        int m = base.ixColumnByVoltageSource.size();
        if (m == 0) {
            return new ComplexNumber[0];
        }

        List<String> voltageSources = new ArrayList<>(base.ixColumnByVoltageSource.keySet());
        ComplexNumber[][] b = new ComplexNumber[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                b[i][j] = ComplexNumber.ZERO;
            }
        }

        for (int j = 0; j < voltageSources.size(); j++) {
            String code = voltageSources.get(j);
            Set<Integer> nodes = AssociaComponentiServlet.buildMembership(AnalysisSessionContext.associations).get(code);
            if (nodes == null) continue;
            for (Integer node : nodes) {
                int sign = AssociaComponentiServlet.voltageSourceSign(
                        AnalysisSessionContext.voltageSourceOrientationByNode,
                        node,
                        code
                );
                b[node][j] = b[node][j].add(new ComplexNumber(sign, 0.0));
            }
        }

        ComplexNumber[] c = new ComplexNumber[n];
        for (int i = 0; i < n; i++) {
            ComplexNumber sum = ComplexNumber.ZERO;
            for (int j = 0; j < n; j++) {
                sum = sum.add(base.kclMatrix[i][j].multiply(eSolution[j]));
            }
            c[i] = sum.subtract(base.currentRhs[i]);
        }

        int[] selectedRows = independentRows(b, m);
        if (selectedRows != null) {
            ComplexNumber[][] square = new ComplexNumber[m][m];
            ComplexNumber[] rhs = new ComplexNumber[m];
            for (int i = 0; i < m; i++) {
                rhs[i] = c[selectedRows[i]];
                for (int j = 0; j < m; j++) {
                    square[i][j] = b[selectedRows[i]][j];
                }
            }
            return solve(square, rhs);
        }

        ComplexNumber[][] normal = new ComplexNumber[m][m];
        ComplexNumber[] d = new ComplexNumber[m];
        for (int i = 0; i < m; i++) {
            d[i] = ComplexNumber.ZERO;
            for (int j = 0; j < m; j++) {
                normal[i][j] = ComplexNumber.ZERO;
            }
        }

        for (int row = 0; row < n; row++) {
            for (int i = 0; i < m; i++) {
                ComplexNumber bi = b[row][i].conjugate();
                d[i] = d[i].add(bi.multiply(c[row]));
                for (int j = 0; j < m; j++) {
                    normal[i][j] = normal[i][j].add(bi.multiply(b[row][j]));
                }
            }
        }

        return solve(normal, d);
    }

    private static int[] independentRows(ComplexNumber[][] matrix, int needed) {
        List<Integer> chosen = new ArrayList<>();
        List<double[]> basis = new ArrayList<>();

        for (int row = 0; row < matrix.length && chosen.size() < needed; row++) {
            double[] candidate = new double[needed];
            for (int col = 0; col < needed; col++) {
                candidate[col] = matrix[row][col].re;
            }
            if (addsRank(basis, candidate)) {
                basis.add(candidate);
                chosen.add(row);
            }
        }

        if (chosen.size() != needed) {
            return null;
        }

        int[] result = new int[needed];
        for (int i = 0; i < needed; i++) {
            result[i] = chosen.get(i);
        }
        return result;
    }

    private static boolean addsRank(List<double[]> basis, double[] candidate) {
        double[] working = candidate.clone();
        for (double[] row : basis) {
            int pivot = firstNonZero(row);
            if (pivot == -1 || Math.abs(working[pivot]) < 1e-9) {
                continue;
            }
            double factor = working[pivot] / row[pivot];
            for (int i = pivot; i < working.length; i++) {
                working[i] -= factor * row[i];
            }
        }

        int pivot = firstNonZero(working);
        if (pivot == -1) {
            return false;
        }
        double scale = working[pivot];
        for (int i = pivot; i < working.length; i++) {
            working[i] /= scale;
        }
        for (double[] row : basis) {
            if (Math.abs(row[pivot]) < 1e-9) {
                continue;
            }
            double factor = row[pivot];
            for (int i = pivot; i < row.length; i++) {
                row[i] -= factor * working[i];
            }
        }
        return true;
    }

    private static int firstNonZero(double[] row) {
        for (int i = 0; i < row.length; i++) {
            if (Math.abs(row[i]) > 1e-9) {
                return i;
            }
        }
        return -1;
    }

    private static NumericSystem nodeSystem(HttpServletRequest req) {
        NodeBaseSystem base = buildNodeBaseSystem(req);
        ComplexNumber[] rawSolution = solve(base.fullMatrix, base.fullRhs);
        ComplexNumber[] eSolution = new ComplexNumber[AnalysisSessionContext.variableNames.size()];
        System.arraycopy(rawSolution, 0, eSolution, 0, eSolution.length);
        ComplexNumber[] ixSolution = computeNodeCurrents(base, eSolution);
        ComplexNumber[] fullSolution = new ComplexNumber[base.unknowns.size()];
        System.arraycopy(eSolution, 0, fullSolution, 0, eSolution.length);

        List<String> voltageSources = new ArrayList<>(base.ixColumnByVoltageSource.keySet());
        for (int i = 0; i < ixSolution.length; i++) {
            String voltageSource = voltageSources.get(i);
            Integer column = base.ixColumnByVoltageSource.get(voltageSource);
            fullSolution[column] = ixSolution[i];
        }
        for (int i = 0; i < fullSolution.length; i++) {
            if (fullSolution[i] == null) {
                fullSolution[i] = ComplexNumber.ZERO;
            }
        }
        return new NumericSystem(base.fullMatrix, base.fullRhs, base.unknowns, fullSolution);
    }

    private static ComplexNumber[] solve(ComplexNumber[][] matrix, ComplexNumber[] rhs) {
        int n = matrix.length;
        int m = matrix[0].length;
        if (n != m) {
            throw new IllegalStateException("Il sistema numerico non e quadrato e non puo essere risolto con il solver attuale.");
        }

        ComplexNumber[][] a = new ComplexNumber[n][n];
        ComplexNumber[] b = new ComplexNumber[n];
        for (int i = 0; i < n; i++) {
            b[i] = rhs[i];
            a[i] = new ComplexNumber[n];
            System.arraycopy(matrix[i], 0, a[i], 0, n);
        }

        for (int col = 0; col < n; col++) {
            int pivot = col;
            double max = a[pivot][col].abs();
            for (int row = col + 1; row < n; row++) {
                double candidate = a[row][col].abs();
                if (candidate > max) {
                    max = candidate;
                    pivot = row;
                }
            }
            if (a[pivot][col].isZero()) {
                throw new IllegalStateException("Sistema singolare: impossibile trovare una soluzione unica.");
            }
            if (pivot != col) {
                ComplexNumber[] tmpRow = a[col];
                a[col] = a[pivot];
                a[pivot] = tmpRow;
                ComplexNumber tmp = b[col];
                b[col] = b[pivot];
                b[pivot] = tmp;
            }

            for (int row = col + 1; row < n; row++) {
                ComplexNumber factor = a[row][col].divide(a[col][col]);
                for (int k = col; k < n; k++) {
                    a[row][k] = a[row][k].subtract(factor.multiply(a[col][k]));
                }
                b[row] = b[row].subtract(factor.multiply(b[col]));
            }
        }

        ComplexNumber[] x = new ComplexNumber[n];
        for (int i = n - 1; i >= 0; i--) {
            ComplexNumber sum = b[i];
            for (int j = i + 1; j < n; j++) {
                sum = sum.subtract(a[i][j].multiply(x[j]));
            }
            x[i] = sum.divide(a[i][i]);
        }
        return x;
    }

    private static String numericMatrixLatex(NumericSystem system) {
        StringBuilder out = new StringBuilder("\\[\\begin{bmatrix}");
        for (int i = 0; i < system.matrix.length; i++) {
            for (int j = 0; j < system.matrix[i].length; j++) {
                out.append(system.matrix[i][j].toLatex());
                if (j < system.matrix[i].length - 1) out.append(" & ");
            }
            out.append(" \\\\ ");
        }
        out.append("\\end{bmatrix}\\,\\begin{bmatrix}");
        for (String unknown : system.unknowns) {
            out.append(AssociaComponentiServlet.latexifyUnknown(unknown)).append(" \\\\ ");
        }
        out.append("\\end{bmatrix} = \\begin{bmatrix}");
        for (ComplexNumber value : system.rhs) {
            out.append(value.toLatex()).append(" \\\\ ");
        }
        out.append("\\end{bmatrix}\\]");
        return out.toString();
    }

    private static String solutionLatex(List<String> unknowns, ComplexNumber[] solution) {
        StringBuilder out = new StringBuilder("\\[\\left\\{\\begin{aligned}");
        for (int i = 0; i < unknowns.size(); i++) {
            out.append(AssociaComponentiServlet.latexifyUnknown(unknowns.get(i)))
                    .append(" &= ")
                    .append(solution[i].toLatex())
                    .append(" \\\\ ");
        }
        out.append("\\end{aligned}\\right.\\]");
        return out.toString();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (AnalysisSessionContext.method == null || AnalysisSessionContext.associations.length == 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Sistema non disponibile. Riparti dalla schermata iniziale.");
            return;
        }

        setBaseAttributes(req);
        req.setAttribute("omegaValue", req.getParameter("omega"));

        try {
            NumericSystem system = AnalysisSessionContext.method == CircuitMethod.MAGLIE
                    ? meshSystem(req)
                    : nodeSystem(req);
            ComplexNumber[] solution = system.presetSolution != null ? system.presetSolution : solve(system.matrix, system.rhs);
            req.setAttribute("numericSystemLatex", numericMatrixLatex(system));
            req.setAttribute("numericSolutionLatex", solutionLatex(system.unknowns, solution));
        } catch (Exception ex) {
            req.setAttribute("solveError", ex.getMessage());
        }

        req.getRequestDispatcher("/sistema.jsp").forward(req, resp);
    }
}

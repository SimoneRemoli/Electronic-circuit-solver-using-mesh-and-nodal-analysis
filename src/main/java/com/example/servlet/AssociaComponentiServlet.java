package com.example.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/AssociaComponentiServlet")
public class AssociaComponentiServlet extends HttpServlet {

    // =============== HELPERS ===============

    /** Impedenza simbolica nel dominio s: R -> Rj, L -> j w Lj, C -> 1/(j w Cj) */
    private static String zTerm(String code) {
        if (code == null || code.isBlank()) return "0";
        char t = code.charAt(0);
        switch (t) {
            case 'R': return code;                      // "R1"
            case 'L': return "j w " + code;             // "j w L1"
            case 'C': return "1/(j w " + code + ")";    // "1/(j w C1)"
            default : return "0";
        }
    }

    private static void addPlus(StringBuilder sb, String term) {
        if (term == null || term.isBlank() || "0".equals(term)) return;
        if (sb.length() > 0) sb.append(" + ");
        sb.append(term);
    }

    private static void addMinus(StringBuilder sb, String term) {
        if (term == null || term.isBlank() || "0".equals(term)) return;
        if (sb.length() > 0) sb.append(" - ");
        else sb.append("- ");
        sb.append(term);
    }

    private static String nz(StringBuilder sb) {
        return (sb == null || sb.length() == 0) ? "0" : sb.toString();
    }

    /** Converte token testuali in LaTeX: R1->R_{1}, j w L2->j\,\omega L_{2}, 1/(j w C3)->\frac{1}{j\,\omega C_{3}} */
    private static String latexifyTerm(String s) {
        if (s == null || s.isBlank()) return "0";
        // ordine importante: prima le frazioni
        s = s.replaceAll("1/\\(j w C(\\d+)\\)", "\\\\frac{1}{j\\\\,\\\\omega C_{$1}}");
        s = s.replaceAll("j w L(\\d+)", "j\\\\,\\\\omega L_{$1}");
        s = s.replaceAll("R(\\d+)", "R_{$1}");
        s = s.replaceAll("V(\\d+)", "V_{$1}");
        s = s.replaceAll("I(\\d+)", "I_{$1}");
        s = s.replaceAll("\\s+", " ").trim();
        return s.isEmpty() ? "0" : s;
    }

    private static String latexOf(StringBuilder sb) {
        return latexifyTerm((sb == null || sb.length() == 0) ? "0" : sb.toString());
    }

    private static String latexCurrentName(List<String> correnti, int i) {
        String def = "I_{" + (i + 1) + "}";
        if (correnti == null || i >= correnti.size()) return def;
        String name = correnti.get(i);
        if (name == null || name.isBlank()) return def;
        return latexifyTerm(name); // "I1" -> I_{1} se l'utente ha scritto così
    }

    private static String latexifyUnknown(String u) {
        if (u == null || u.isBlank()) return "0";
        if (u.matches("^I\\d+$")) {           // I1 -> I_{1}
            return u.replaceAll("I(\\d+)", "I_{$1}");
        }
        if (u.matches("^Vx\\d+$")) {          // Vx1 -> V_{x,1}
            return u.replaceAll("Vx(\\d+)", "V_{x,$1}");
        }
        return latexifyTerm(u);
    }

    /** toLatex con M colonne (N correnti + S Vx). unknowns.size() deve essere M. */
    private static String toLatex(StringBuilder[][] A, List<String> unknowns, StringBuilder[] b) {
        int N = A.length;
        int M = (N == 0 ? 0 : A[0].length);

        StringBuilder out = new StringBuilder();

        // Matrice Z estesa
        out.append("\\[\\begin{bmatrix}");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                out.append(latexOf(A[i][j]));
                if (j < M - 1) out.append(" & ");
            }
            out.append(" \\\\ ");
        }
        out.append("\\end{bmatrix}\\,");

        // Vettore delle incognite [I; Vx]
        out.append("\\begin{bmatrix}");
        for (int j = 0; j < M; j++) {
            out.append(latexifyUnknown(unknowns.get(j))).append(" \\\\ ");
        }
        out.append("\\end{bmatrix} = ");

        // Vettore noto [E]
        out.append("\\begin{bmatrix}");
        for (int i = 0; i < N; i++) out.append(latexOf(b[i])).append(" \\\\ ");
        out.append("\\end{bmatrix}\\]");

        // Forma espansa
        out.append("\n\\[\\begin{aligned}");
        for (int i = 0; i < N; i++) {
            boolean first = true;
            for (int j = 0; j < M; j++) {
                String coeff = latexOf(A[i][j]);
                if ("0".equals(coeff)) continue;
                if (!first) out.append(" + ");
                out.append("(").append(coeff).append(")\\,").append(latexifyUnknown(unknowns.get(j)));
                first = false;
            }
            if (first) out.append("0");
            out.append(" &= ").append(latexOf(b[i])).append(" \\\\ ");
        }
        out.append("\\end{aligned}\\]");

        return out.toString();
    }

    // =============== SERVLET ===============

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int N = ImageUploadServlet.numero_correnti_maglia;
        List<String> direzioni = ImageUploadServlet.direzioni_correnti_maglia;   // "CW"/"CCW"
        List<String> correnti  = ImageUploadServlet.correnti_di_maglia;          // nomi I1, I2, ...

        if (N <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "N (numero maglie) non valido: " + N);
            return;
        }

        // 1) Leggo i componenti selezionati e riempio m[i][k]
        String[][] m = new String[N][100];
        for (int i = 0; i < N; i++) {
            int col = 0;
            String[] R = req.getParameterValues("mesh" + i + "_R");
            String[] L = req.getParameterValues("mesh" + i + "_L");
            String[] C = req.getParameterValues("mesh" + i + "_C");
            String[] I = req.getParameterValues("mesh" + i + "_I");
            String[] V = req.getParameterValues("mesh" + i + "_V");

            if (R != null) for (String s : R) m[i][col++] = s;
            if (L != null) for (String s : L) m[i][col++] = s;
            if (C != null) for (String s : C) m[i][col++] = s;
            if (I != null) for (String s : I) m[i][col++] = s;
            if (V != null) for (String s : V) m[i][col++] = s;
        }

        // 2) Mappa "componente -> maglie in cui compare"
        Map<String, List<Integer>> belongs = new LinkedHashMap<>();
        for (int i = 0; i < N; i++) {
            for (int k = 0; k < m[i].length; k++) {
                String code = Objects.toString(m[i][k], "");
                if (code.isEmpty()) continue;
                belongs.computeIfAbsent(code, c -> new ArrayList<>()).add(i);
            }
        }

        // 3) Per ogni sorgente di corrente Ik creo una incognita Vxk
        LinkedHashMap<String, Integer> vxMap = new LinkedHashMap<>();
        for (String code : belongs.keySet()) {
            if (code != null && !code.isBlank() && code.charAt(0) == 'I') {
                vxMap.putIfAbsent(code, vxMap.size());
            }
        }
        int S = vxMap.size();       // quante Vx
        int M = N + S;              // incognite totali (I + Vx)

        // 4) Matrice A (N x M) e RHS b (N)
        StringBuilder[][] A = new StringBuilder[N][M];
        StringBuilder[] b = new StringBuilder[N];
        for (int i = 0; i < N; i++) {
            b[i] = new StringBuilder();
            for (int j = 0; j < M; j++) A[i][j] = new StringBuilder();
        }

        List<String> vincoliCorrente = new ArrayList<>();

        // 5) Popolamento A e b
        for (var e : belongs.entrySet()) {
            String code = e.getKey();
            List<Integer> meshes = e.getValue();
            char t = code.charAt(0);

            if (t == 'R' || t == 'L' || t == 'C') {
                String Z = zTerm(code);
                // Diagonale
                for (int mi : meshes) addPlus(A[mi][mi], Z);
                // Off-diagonali: stesso verso => -, verso differente => +
                for (int i = 0; i < meshes.size(); i++) {
                    for (int j = 0; j < meshes.size(); j++) {
                        if (i == j) continue;
                        int mi = meshes.get(i), mj = meshes.get(j);
                        boolean sameDir = false;
                        try {
                            sameDir = direzioni != null
                                    && direzioni.get(mi) != null
                                    && direzioni.get(mi).equalsIgnoreCase(direzioni.get(mj));
                        } catch (Exception ignore) {}
                        if (sameDir) addMinus(A[mi][mj], Z);
                        else         addPlus (A[mi][mj], Z);
                    }
                }
            } else if (t == 'V') {
                // Generatore di tensione noto: va nel RHS delle maglie in cui compare
                for (int mi : meshes) addPlus(b[mi], code);
            } else if (t == 'I') {
                // Generatore di corrente: crea incognita Vxk e coeff. +/-1 sulle righe delle maglie
                Integer sIdx = vxMap.get(code);
                if (sIdx != null) {
                    int colVx = N + sIdx; // posizione colonna Vxk
                    if (meshes.size() == 1) {
                        int mi = meshes.get(0);
                        addPlus(A[mi][colVx], "1");       // +Vxk in quella maglia
                    } else if (meshes.size() == 2) {
                        int a = meshes.get(0), c = meshes.get(1);
                        addPlus (A[a][colVx], "1");       // +Vxk in una
                        addMinus(A[c][colVx], "1");       // -Vxk nell'altra
                    } else {
                        // caso raro: tocca più maglie -> metto +1 ovunque per semplicità
                        for (int mi : meshes) addPlus(A[mi][colVx], "1");
                    }
                }

                // Vincoli sulle correnti di maglia dovuti a Ik (nota)
                String idx = code.substring(1);
                if (meshes.size() == 1) {
                    int mi = meshes.get(0);
                    String Ii = (correnti != null && mi < correnti.size() && correnti.get(mi) != null && !correnti.get(mi).isBlank())
                            ? correnti.get(mi) : ("I" + (mi + 1));
                    vincoliCorrente.add(Ii + " = I" + idx);
                } else if (meshes.size() == 2) {
                    int a = meshes.get(0), c = meshes.get(1);
                    String Ia = (correnti != null && a < correnti.size() && correnti.get(a) != null && !correnti.get(a).isBlank())
                            ? correnti.get(a) : ("I" + (a + 1));
                    String Ic = (correnti != null && c < correnti.size() && correnti.get(c) != null && !correnti.get(c).isBlank())
                            ? correnti.get(c) : ("I" + (c + 1));
                    vincoliCorrente.add(Ia + " - " + Ic + " = I" + idx);
                } else {
                    vincoliCorrente.add("// Vincolo su " + code + " tra maglie " + meshes);
                }
            }
        }

        // 6) Costruisco la lista delle incognite [I... , Vx...]
        List<String> unknowns = new ArrayList<>(M);
        for (int i = 0; i < N; i++) {
            String Ii = (correnti != null && i < correnti.size() && correnti.get(i) != null && !correnti.get(i).isBlank())
                    ? correnti.get(i) : ("I" + (i + 1));
            unknowns.add(Ii);
        }
        for (String ik : vxMap.keySet()) {
            unknowns.add(ik.replaceFirst("^I", "Vx"));  // I3 -> Vx3
        }

        // 7) LaTeX sistema
        String latexSystem = toLatex(A, unknowns, b);

        // 8) Vincoli (Ik note) in LaTeX
        String latexConstraints = null;
        if (!vincoliCorrente.isEmpty()) {
            StringBuilder cons = new StringBuilder();
            cons.append("\\[\\begin{aligned}");
            for (String c : vincoliCorrente) cons.append(latexifyTerm(c)).append(" \\\\ ");
            cons.append("\\end{aligned}\\]");
            latexConstraints = cons.toString();
        }

        // 9) (Opzionale) equazioni testuali con le nuove incognite
        String[] eqTesto = new String[N];
        for (int i = 0; i < N; i++) {
            StringBuilder row = new StringBuilder("[ ");
            boolean first = true;
            for (int j = 0; j < M; j++) {
                String coeff = nz(A[i][j]);
                if ("0".equals(coeff)) continue;
                if (!first) row.append(" + ");
                String unk = unknowns.get(j);
                row.append("(").append(coeff).append(") * ").append(unk);
                first = false;
            }
            if (first) row.append("0");
            row.append(" ] = ").append(nz(b[i]));
            eqTesto[i] = row.toString();
        }

        // 10) Attributi e forward UNA sola volta
        req.setAttribute("latexSystem", latexSystem);
        if (latexConstraints != null) req.setAttribute("latexConstraints", latexConstraints);
        req.setAttribute("equazioniTesto", eqTesto);
        req.getRequestDispatcher("/sistema.jsp").forward(req, resp);
    }
}

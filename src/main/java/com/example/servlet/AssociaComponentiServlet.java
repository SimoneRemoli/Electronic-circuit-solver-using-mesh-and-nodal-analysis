package com.example.servlet;

import javax.servlet.RequestDispatcher;
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

    /** Aggiunge " + term" gestendo lo spazio/segno solo se serve */
    private static void addPlus(StringBuilder sb, String term) {
        if (term == null || term.isBlank() || "0".equals(term)) return;
        if (sb.length() > 0) sb.append(" + ");
        sb.append(term);
    }

    /** Aggiunge " - term" gestendo lo spazio/segno solo se serve */
    private static void addMinus(StringBuilder sb, String term) {
        if (term == null || term.isBlank() || "0".equals(term)) return;
        if (sb.length() > 0) sb.append(" - ");
        else sb.append("- ");
        sb.append(term);
    }

    /** Se vuoto -> "0" */
    private static String nz(StringBuilder sb) {
        return (sb == null || sb.length() == 0) ? "0" : sb.toString();
    }

    /** Converte i tuoi token testuali in LaTeX: R1->R_{1}, j w L2->j\,\omega L_{2}, 1/(j w C3)->\frac{1}{j\,\omega C_{3}} */
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

    /** Se lo StringBuilder è vuoto → "0", altrimenti latexify */
    private static String latexOf(StringBuilder sb) {
        return latexifyTerm((sb == null || sb.length() == 0) ? "0" : sb.toString());
    }

    /** Nome corrente in LaTeX (usa il nome utente se presente, altrimenti I_{i+1}) */
    private static String latexCurrentName(List<String> correnti, int i) {
        String def = "I_{" + (i + 1) + "}";
        if (correnti == null || i >= correnti.size()) return def;
        String name = correnti.get(i);
        if (name == null || name.isBlank()) return def;
        return latexifyTerm(name); // es. "I1" -> I_{1}
    }

    /** Costruisce la forma matriciale e quella espansa in LaTeX: [Z][I]=[E] + equazioni */
    private static String toLatex(StringBuilder[][] A, StringBuilder[] b, List<String> correnti) {
        int N = b.length;

        // Matrice Z
        StringBuilder out = new StringBuilder();
        out.append("\\[\\begin{bmatrix}");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                out.append(latexOf(A[i][j]));
                if (j < N - 1) out.append(" & ");
            }
            out.append(" \\\\ ");
        }
        out.append("\\end{bmatrix}\\,");
        // Vettore I
        out.append("\\begin{bmatrix}");
        for (int i = 0; i < N; i++) out.append(latexCurrentName(correnti, i)).append(" \\\\ ");
        out.append("\\end{bmatrix} = ");
        // Vettore E
        out.append("\\begin{bmatrix}");
        for (int i = 0; i < N; i++) out.append(latexOf(b[i])).append(" \\\\ ");
        out.append("\\end{bmatrix}\\]");

        // Forma espansa
        out.append("\n\\[\\begin{aligned}");
        for (int i = 0; i < N; i++) {
            String Ii = latexCurrentName(correnti, i);
            out.append("(").append(latexOf(A[i][i])).append(")\\,").append(Ii);
            for (int j = 0; j < N; j++) if (j != i) {
                String off = latexOf(A[i][j]);
                if (!"0".equals(off)) out.append(" + (").append(off).append(")\\,").append(latexCurrentName(correnti, j));
            }
            out.append(" &= ").append(latexOf(b[i])).append(" \\\\ ");
        }
        out.append("\\end{aligned}\\]");

        return out.toString();
    }


    // =============== SERVLET ===============

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1) Dati globali dalla prima servlet (come da tua scelta)
        int N = ImageUploadServlet.numero_correnti_maglia;
        List<String> direzioni = ImageUploadServlet.direzioni_correnti_maglia;   // "CW"/"CCW"
        List<String> correnti  = ImageUploadServlet.correnti_di_maglia;          // nomi I1, I2, ...

        if (N <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "N (numero maglie) non valido: " + N);
            return;
        }

        // 2) Leggo i componenti selezionati e riempio m[i][k]
        String[][] m = new String[N][100];             // tua struttura
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

        // 3) Mappa "componente -> maglie in cui compare"
        Map<String, List<Integer>> belongs = new LinkedHashMap<>();
        for (int i = 0; i < N; i++) {
            for (int k = 0; k < m[i].length; k++) {
                String code = Objects.toString(m[i][k], "");
                if (code.isEmpty()) continue;
                belongs.computeIfAbsent(code, c -> new ArrayList<>()).add(i);
            }
        }

        // 4) Costruisco A (Z_ij) e b (RHS)
        StringBuilder[][] A = new StringBuilder[N][N];
        StringBuilder[] b = new StringBuilder[N];
        for (int i = 0; i < N; i++) {
            b[i] = new StringBuilder();
            for (int j = 0; j < N; j++) A[i][j] = new StringBuilder();
        }

        List<String> vincoliCorrente = new ArrayList<>();

        for (var e : belongs.entrySet()) {
            String code = e.getKey();
            List<Integer> meshes = e.getValue();
            char t = code.charAt(0);

            if (t == 'R' || t == 'L' || t == 'C') {
                String Z = zTerm(code);
                // +Z su ogni diagonale dove compare
                for (int mi : meshes) addPlus(A[mi][mi], Z);
                // ±Z sugli off-diagonali tra maglie che condividono il componente
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
                        if (sameDir) addMinus(A[mi][mj], Z);  // STESSO verso => -
                        else         addPlus(A[mi][mj], Z);   // verso DIFFERENTE => +
                    }
                }
            } else if (t == 'V') {
                // generatore di tensione: va in RHS della/e maglia/e dove compare (segno semplificato +)
                for (int mi : meshes) addPlus(b[mi], code); // es. "V1"
            } else if (t == 'I') {
                // generatore di corrente: vincoli esterni
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

        String latexSystem = toLatex(A, b, correnti);

// (opzionale) vincoli da generatori di corrente in LaTeX
        String latexConstraints = null;
        if (!vincoliCorrente.isEmpty()) {
            StringBuilder cons = new StringBuilder();
            cons.append("\\[\\begin{aligned}");
            for (String c : vincoliCorrente) cons.append(latexifyTerm(c)).append(" \\\\ ");
            cons.append("\\end{aligned}\\]");
            latexConstraints = cons.toString();
        }

// manda a JSP che renderizza MathJax
        req.setAttribute("latexSystem", latexSystem);
        if (latexConstraints != null) req.setAttribute("latexConstraints", latexConstraints);
        req.getRequestDispatcher("/sistema.jsp").forward(req, resp);

// se preferisci, mantieni anche le tue equazioni testuali in parallelo

        // 5) Costruisco le equazioni complete ( pattern: metto tutto in equazioni[i][0])
        String[][] equazioni = new String[N][1];
        for (int i = 0; i < N; i++) {
            String Ii = (correnti != null && i < correnti.size() && correnti.get(i) != null && !correnti.get(i).isBlank())
                    ? correnti.get(i) : ("I" + (i + 1));

            StringBuilder row = new StringBuilder();
            row.append("[ (").append(nz(A[i][i])).append(") * ").append(Ii);
            for (int j = 0; j < N; j++) {
                if (j == i) continue;
                String off = nz(A[i][j]);
                if (!"0".equals(off)) {
                    String Ij = (correnti != null && j < correnti.size() && correnti.get(j) != null && !correnti.get(j).isBlank())
                            ? correnti.get(j) : ("I" + (j + 1));
                    row.append(" + (").append(off).append(") * ").append(Ij);
                }
            }
            row.append(" ] = ").append(nz(b[i]));
            equazioni[i][0] = row.toString();
        }

        // 6) Debug/Output
        System.out.println("=== SISTEMA DELLE MAGLIE ===");
        for (int i = 0; i < N; i++) System.out.println(equazioni[i][0]);
        if (!vincoliCorrente.isEmpty()) {
            System.out.println("--- VINCOLI (sorgenti di corrente) ---");
            for (String s : vincoliCorrente) System.out.println(s);
        }

        // 7) Verso JSP (se vuoi visualizzarlo)
        req.setAttribute("equazioni", equazioni);
        req.setAttribute("vincoli", vincoliCorrente);
        // req.getRequestDispatcher("/equazioni.jsp").forward(req, resp);
    }
}

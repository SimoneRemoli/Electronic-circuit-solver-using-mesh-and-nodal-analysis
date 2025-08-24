package com.example.servlet;
import java.text.Normalizer;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_imgproc.Vec4iVector;

import javax.imageio.ImageIO;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class CircuitParsingService {

    /** Entry point: ritorna nodi, rami (fili + componenti) e lista componenti. */
    public CircuitParseResult parse(Path imagePath) {
        Mat gray = loadGray(imagePath);
        if (gray == null || gray.empty()) {
            return new CircuitParseResult(List.of(), List.of(), List.of(), 0, 0, 0);
        }

        // --- Preproc ---
        Mat eq = new Mat();
        equalizeHist(gray, eq);
        GaussianBlur(eq, eq, new org.bytedeco.opencv.opencv_core.Size(3,3), 0);

        Mat edges = new Mat();
        Canny(eq, edges, 50, 150, 3, false);

        // --- Hough (usa Vec4iVector) ---
        Vec4iVector lines = detectLinesAdaptive(edges, gray.cols(), gray.rows());

        // --- Nodi e rami ---
        List<Point> endpoints = collectEndpoints(lines);
        double eps = Math.max(5, Math.min(gray.cols(), gray.rows()) * 0.02);
        List<Point> nodes = clusterPoints(endpoints, eps);

        double snap = eps * 1.2;
        List<int[]> branches = mapSegmentsToBranches(lines, nodes, snap);

        // --- OCR etichette -> componenti ---
        List<CircuitComponent> comps = detectComponentsByOCR(imagePath.toFile(), nodes);

        // componenti come archi
        for (CircuitComponent c : comps) {
            branches.add(new int[]{Math.min(c.nodeA, c.nodeB), Math.max(c.nodeA, c.nodeB)});
        }

        // --- dedupe e misure del grafo ---
        List<int[]> uniqueBranches = dedupBranches(branches);
        int N = nodes.size();
        int E = uniqueBranches.size();
        int C = countConnectedComponents(N, uniqueBranches);
        int M = Math.max(0, E - N + C);

        return new CircuitParseResult(nodes, uniqueBranches, comps, N, M, C);
    }

    // =====================================================================
    // OpenCV helpers
    // =====================================================================
    private Mat loadGray(Path p) {
        Loader.load(org.bytedeco.opencv.global.opencv_core.class);
        Mat src = imread(p.toString(), IMREAD_GRAYSCALE);
        if (src == null || src.empty()) return new Mat();
        return src;
    }

    /** Hough adattivo: prova 3 set finché trova linee. */
    private Vec4iVector detectLinesAdaptive(Mat edges, int w, int h) {
        double minDim = Math.min(w, h);
        int[][] tries = new int[][]{
                {60, (int)(0.15*minDim), (int)(0.02*minDim)},
                {40, (int)(0.10*minDim), (int)(0.02*minDim)},
                {25, (int)(0.06*minDim), (int)(0.015*minDim)}
        };
        for (int[] t : tries) {
            Vec4iVector lines = new Vec4iVector();
            HoughLinesP(edges, lines, 1, Math.PI/180, t[0], t[1], t[2]);
            if (lines.size() > 0) return lines;
        }
        return new Vec4iVector(); // niente trovato
    }

    /** Estrai gli estremi dai segmenti (Vec4iVector: [x1,y1,x2,y2] per entry). */
    private List<Point> collectEndpoints(Vec4iVector lines) {
        List<Point> pts = new ArrayList<>();
        for (long i = 0; i < lines.size(); i++) {
            // Vec4i estende IntPointer: non serve importare Vec4i, uso IntPointer
            IntPointer v = lines.get(i);
            int x1 = v.get(0), y1 = v.get(1), x2 = v.get(2), y2 = v.get(3);
            pts.add(new Point(x1, y1));
            pts.add(new Point(x2, y2));
        }
        return pts;
    }

    private List<Point> clusterPoints(List<Point> pts, double eps) {
        List<Point> centers = new ArrayList<>();
        for (Point p : pts) {
            int idx = findCluster(centers, p, eps);
            if (idx < 0) centers.add(new Point(p));
            else {
                Point c = centers.get(idx);
                c.x = (c.x + p.x) / 2;
                c.y = (c.y + p.y) / 2;
            }
        }
        return centers;
    }

    private int findCluster(List<Point> centers, Point p, double eps) {
        for (int i = 0; i < centers.size(); i++) {
            if (p.distance(centers.get(i)) <= eps) return i;
        }
        return -1;
    }

    private int nearestNode(List<Point> nodes, Point p) {
        int best = -1; double bd = Double.MAX_VALUE;
        for (int i = 0; i < nodes.size(); i++) {
            double d = p.distance(nodes.get(i));
            if (d < bd) { bd = d; best = i; }
        }
        return best;
    }

    private int secondNearestNode(List<Point> nodes, Point p, int excludeIndex) {
        int best = -1; double bd = Double.MAX_VALUE;
        for (int i = 0; i < nodes.size(); i++) {
            if (i == excludeIndex) continue;
            double d = p.distance(nodes.get(i));
            if (d < bd) { bd = d; best = i; }
        }
        return best;
    }

    /** Converte i segmenti in archi (nodeA,nodeB) agganciando ai nodi più vicini. */
    private List<int[]> mapSegmentsToBranches(Vec4iVector lines, List<Point> nodes, double snapDist) {
        List<int[]> list = new ArrayList<>();
        if (nodes.isEmpty() || lines.size() == 0) return list;

        for (long i = 0; i < lines.size(); i++) {
            IntPointer v = lines.get(i);
            Point p1 = new Point(v.get(0), v.get(1));
            Point p2 = new Point(v.get(2), v.get(3));
            int a = nearestNode(nodes, p1);
            int b = nearestNode(nodes, p2);
            if (a >= 0 && b >= 0 && a != b) {
                if (p1.distance(nodes.get(a)) <= snapDist && p2.distance(nodes.get(b)) <= snapDist) {
                    int n1 = Math.min(a, b), n2 = Math.max(a, b);
                    list.add(new int[]{n1, n2});
                }
            }
        }
        return list;
    }

    private List<int[]> dedupBranches(List<int[]> branches) {
        Set<String> set = new HashSet<>();
        List<int[]> out = new ArrayList<>();
        for (int[] e : branches) {
            int a = Math.min(e[0], e[1]), b = Math.max(e[0], e[1]);
            String k = a + "-" + b;
            if (set.add(k)) out.add(new int[]{a, b});
        }
        return out;
    }

    private int countConnectedComponents(int N, List<int[]> edges) {
        List<List<Integer>> g = new ArrayList<>(N);
        for (int i = 0; i < N; i++) g.add(new ArrayList<>());
        for (int[] e : edges) {
            g.get(e[0]).add(e[1]);
            g.get(e[1]).add(e[0]);
        }
        boolean[] vis = new boolean[N];
        int cc = 0;
        for (int i = 0; i < N; i++) {
            if (!vis[i]) { cc++; bfs(i, g, vis); }
        }
        return cc;
    }

    private void bfs(int s, List<List<Integer>> g, boolean[] vis) {
        ArrayDeque<Integer> dq = new ArrayDeque<>();
        dq.add(s); vis[s] = true;
        while (!dq.isEmpty()) {
            int u = dq.poll();
            for (int v : g.get(u)) if (!vis[v]) { vis[v] = true; dq.add(v); }
        }
    }
    private String normalizeLabel(String s) {
        if (s == null) return "";
        // normalizza Unicode (NFKC) e porta a maiuscolo
        String t = Normalizer.normalize(s, Normalizer.Form.NFKC).toUpperCase(Locale.ROOT);

        // rimpiazzo pedici Unicode con cifre normali
        t = t.replace('₀','0').replace('₁','1').replace('₂','2').replace('₃','3')
                .replace('₄','4').replace('₅','5').replace('₆','6').replace('₇','7')
                .replace('₈','8').replace('₉','9');

        // rimuovi spazi e simboli non alfanumerici (teniamo solo [A-Z0-9])
        t = t.replaceAll("[^A-Z0-9]", "");

        // correzioni comuni: I scambiata per 1 dopo R/C/L/D
        t = t.replaceAll("(^|[^A-Z])RI(\\d+)", "$11$2")
                .replaceAll("(^|[^A-Z])CI(\\d+)", "$11$2")
                .replaceAll("(^|[^A-Z])LI(\\d+)", "$11$2")
                .replaceAll("(^|[^A-Z])DI(\\d+)", "$11$2");

        // etichette con g/s: Vg3, Ig2 → VG3, IG2 (gli spazi li abbiamo già tolti)
        // (già sistemato dal toUpperCase + remove spaces)
        return t;
    }


    // =====================================================================
    // OCR (robusto: non crasha se manca Tesseract nativo)
    // =====================================================================
    private List<CircuitComponent> detectComponentsByOCR(File imageFile, List<Point> nodes) {
        List<CircuitComponent> comps = new ArrayList<>();

        if ("false".equalsIgnoreCase(System.getProperty("ocr.enabled", "true"))) {
            return comps; // disattivato volontariamente
        }

        try {
            ITesseract t = new Tesseract();
            String dp = System.getProperty("tessdata.dir");
            if (dp != null) t.setDatapath(dp);
            t.setLanguage("eng");

            BufferedImage bi = ImageIO.read(imageFile);
            List<Word> words = t.getWords(bi, ITessAPI.TessPageIteratorLevel.RIL_WORD);

            for (Word w : words) {
                String raw = (w.getText() == null) ? "" : w.getText();
                String norm = normalizeLabel(raw);

                // DEBUG: stampa cosa sta leggendo
                System.out.println("OCR raw='" + raw + "' -> norm='" + norm + "'");

                if (!looksLikeLabel(norm)) continue;

                ComponentType type = classify(norm);

                // centro della bbox
                java.awt.Rectangle r = w.getBoundingBox();
                Point center = new Point(r.x + r.width / 2, r.y + r.height / 2);

                int a = nearestNode(nodes, center);
                int b = secondNearestNode(nodes, center, a);
                if (a >= 0 && b >= 0 && a != b) {
                    int n1 = Math.min(a, b), n2 = Math.max(a, b);
                    // salvo la label normalizzata (più facile da usare poi)
                    comps.add(new CircuitComponent(type, n1, n2, norm));
                }
            }
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            System.err.println("OCR disabilitato (native mancanti): " + e.getMessage());
        } catch (Exception ex) {
            System.err.println("OCR errore non critico: " + ex.getMessage());
        }
        return dedupComponents(comps);
    }


    private boolean looksLikeLabel(String normalized) {
        String s = (normalized == null) ? "" : normalized;

        // Res, Cap, Ind, Diodi numerati
        if (s.matches("R\\d+|C\\d+|L\\d+|D\\d+")) return true;

        // Sorgenti di tensione: VGx, VSx o Vx
        if (s.matches("VG\\d+|VS\\d+|V\\d+")) return true;

        // Sorgenti di corrente: IGx o Ix
        if (s.matches("IG\\d+|I\\d+")) return true;

        return false;
    }


    private ComponentType classify(String normalized) {
        String s = (normalized == null) ? "" : normalized;

        if (s.startsWith("R")) return ComponentType.RESISTOR;
        if (s.startsWith("C")) return ComponentType.CAPACITOR;
        if (s.startsWith("L")) return ComponentType.INDUCTOR;
        if (s.startsWith("D")) return ComponentType.DIODE;

        if (s.startsWith("VG") || s.startsWith("VS") || s.matches("V\\d+"))
            return ComponentType.V_SOURCE;

        if (s.startsWith("IG") || s.matches("I\\d+"))
            return ComponentType.I_SOURCE;

        return ComponentType.UNKNOWN;
    }


    // =====================================================================
    // Dedupe componenti (stessa etichetta/tipo e stessi nodi)
    // =====================================================================
    private List<CircuitComponent> dedupComponents(List<CircuitComponent> comps) {
        if (comps == null || comps.isEmpty()) return Collections.emptyList();
        Set<String> seen = new LinkedHashSet<>();
        List<CircuitComponent> out = new ArrayList<>();
        for (CircuitComponent c : comps) {
            String key = c.type + ":" +
                    (c.label == null ? "" : c.label.toUpperCase(Locale.ROOT)) + ":" +
                    Math.min(c.nodeA, c.nodeB) + ":" + Math.max(c.nodeA, c.nodeB);
            if (seen.add(key)) out.add(c);
        }
        return out;
    }
}

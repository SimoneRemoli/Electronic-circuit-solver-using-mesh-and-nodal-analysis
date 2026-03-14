package com.example.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@WebServlet("/ImageUploadServlet")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 20 * 1024 * 1024, fileSizeThreshold = 0)
public class ImageUploadServlet extends HttpServlet {

    private Path uploadDir;

    @Override
    public void init() throws ServletException {
        try {
            String base = System.getProperty("circuiti.upload.dir", System.getProperty("java.io.tmpdir"));
            uploadDir = Path.of(base, "circuiti-uploads");
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new ServletException("Impossibile creare la cartella di upload", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String methodStr = req.getParameter("method");
        int numeroResistenze = Integer.parseInt(req.getParameter("count"));
        int numeroInduttanze = Integer.parseInt(req.getParameter("count2"));
        int numeroCondensatori = Integer.parseInt(req.getParameter("count3"));
        int numeroGeneratoriCorrente = Integer.parseInt(req.getParameter("count4"));
        int numeroGeneratoriTensione = Integer.parseInt(req.getParameter("count5"));
        int equationCount = Integer.parseInt(req.getParameter("entityCount"));
        String referenceNodeName = req.getParameter("referenceNodeName");

        CircuitMethod method;
        try {
            method = CircuitMethod.valueOf(Objects.requireNonNull(methodStr));
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Metodo non valido");
            return;
        }

        List<String> variableNames = new ArrayList<>();
        List<String> meshDirections = new ArrayList<>();
        for (int i = 0; i < equationCount; i++) {
            String rawName = req.getParameter("entityNames" + (i + 1));
            String fallback = method == CircuitMethod.MAGLIE ? "I" + (i + 1) : "E" + (i + 1);
            variableNames.add((rawName == null || rawName.isBlank()) ? fallback : rawName.trim());
        }
        if (method == CircuitMethod.MAGLIE) {
            for (int i = 0; i < equationCount; i++) {
                String direction = req.getParameter("meshDir" + (i + 1));
                meshDirections.add((direction == null || direction.isBlank()) ? "CW" : direction);
            }
        }

        AnalysisSessionContext.reset();
        AnalysisSessionContext.method = method;
        AnalysisSessionContext.equationCount = equationCount;
        AnalysisSessionContext.numeroResistenze = numeroResistenze;
        AnalysisSessionContext.numeroInduttanze = numeroInduttanze;
        AnalysisSessionContext.numeroCondensatori = numeroCondensatori;
        AnalysisSessionContext.numeroGeneratoriCorrente = numeroGeneratoriCorrente;
        AnalysisSessionContext.numeroGeneratoriTensione = numeroGeneratoriTensione;
        AnalysisSessionContext.variableNames = variableNames;
        AnalysisSessionContext.meshDirections = meshDirections;
        AnalysisSessionContext.referenceNodeName = (referenceNodeName == null || referenceNodeName.isBlank()) ? "GND" : referenceNodeName.trim();

        req.setAttribute("method", method.name());
        req.setAttribute("valori_resistenze", numeroResistenze);
        req.setAttribute("valori_induttanze", numeroInduttanze);
        req.setAttribute("valori_condensatori", numeroCondensatori);
        req.setAttribute("valori_generatori_corrente", numeroGeneratoriCorrente);
        req.setAttribute("valori_generatori_tensione", numeroGeneratoriTensione);
        req.setAttribute("entityCount", equationCount);
        req.setAttribute("variableNames", variableNames);
        req.setAttribute("meshDirections", meshDirections);
        req.setAttribute("referenceNodeName", AnalysisSessionContext.referenceNodeName);

        String debugName = System.getProperty("last.debug.filename");
        if (debugName != null) {
            req.setAttribute("debugFileName", debugName);
        }

        System.out.println(" | " + method.name() + " | " + numeroResistenze);
        for (int i = 0; i < equationCount; i++) {
            System.out.println("Variabile " + variableNames.get(i));
            if (method == CircuitMethod.MAGLIE) {
                System.out.println("Direzione: " + meshDirections.get(i));
            }
        }

        RequestDispatcher dispatcher = req.getRequestDispatcher("/result.jsp");
        dispatcher.forward(req, resp);
    }
}

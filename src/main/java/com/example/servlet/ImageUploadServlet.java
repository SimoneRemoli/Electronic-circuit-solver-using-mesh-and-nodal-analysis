package com.example.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@WebServlet("/ImageUploadServlet")
@MultipartConfig(maxFileSize = 10*1024*1024, maxRequestSize = 20*1024*1024, fileSizeThreshold = 0)

public class ImageUploadServlet extends HttpServlet {

    private Path uploadDir;

    @Override
    public void init() throws ServletException {
        try {
            // Cartella esterna configurabile: -Dcircuiti.upload.dir=/percorso
            String base = System.getProperty("circuiti.upload.dir",
                    System.getProperty("java.io.tmpdir"));
            uploadDir = Path.of(base, "circuiti-uploads");
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new ServletException("Impossibile creare la cartella di upload", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String methodStr = req.getParameter("method");
        int numero_resistenze = Integer.parseInt(req.getParameter("count"));
        int numero_induttanze = Integer.parseInt(req.getParameter("count2"));
        int numero_condensatori = Integer.parseInt(req.getParameter("count3"));
        int numero_gen_corrente = Integer.parseInt(req.getParameter("count4"));
        int numero_gen_tensione = Integer.parseInt(req.getParameter("count5"));
        int numero_correnti_maglia = Integer.parseInt(req.getParameter("meshCount")); //null se check nodi


        List<String> valori_resistenze = new ArrayList<>();
        List<String> valori_induttanze = new ArrayList<>();
        List<String> valori_condensatori = new ArrayList<>();
        List<String> valori_generatori_corrente = new ArrayList<>();
        List<String> valori_generatori_tensione = new ArrayList<>();
        List<String> correnti_di_maglia = new ArrayList<>();
        List<String> direzioni_correnti_maglia = new ArrayList<>();
        for(int i=0;i<numero_resistenze;i++)
        {
            valori_resistenze.add(i, req.getParameter("field"+(i+1)));
        }
        for(int i=0;i<numero_induttanze;i++)
        {
            valori_induttanze.add(i, req.getParameter("field2"+(i+1)));
        }
        for(int i=0;i<numero_condensatori;i++)
        {
            valori_condensatori.add(i, req.getParameter("field3"+(i+1)));
        }
        for(int i=0;i<numero_gen_corrente;i++)
        {
            valori_generatori_corrente.add(i, req.getParameter("field4"+(i+1)));
        }
        for(int i=0;i<numero_gen_tensione;i++)
        {
            valori_generatori_tensione.add(i, req.getParameter("field5"+(i+1)));
        }
        for(int i=0;i<numero_correnti_maglia;i++)
        {
            correnti_di_maglia.add(i, req.getParameter("meshNames"+(i+1)));
        }
        for(int i=0;i<numero_correnti_maglia;i++)
        {
            direzioni_correnti_maglia.add(i, req.getParameter("meshDir"+(i+1)));
        }




        CircuitMethod method;
        try {
            method = CircuitMethod.valueOf(Objects.requireNonNull(methodStr));
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Metodo non valido");
            return;
        }







        req.setAttribute("method", method.name());
        req.setAttribute("valori_resistenze", valori_resistenze);
        req.setAttribute("valori_induttanze", valori_induttanze);
        req.setAttribute("valori_condensatori", valori_condensatori);
        req.setAttribute("valori_generatori_corrente", valori_generatori_corrente);
        req.setAttribute("valori_generatori_tensione", valori_generatori_tensione);
        req.setAttribute("correnti_di_maglia", correnti_di_maglia);
        req.setAttribute("direzioni_correnti_maglia", direzioni_correnti_maglia);


        // prendi il nome del PNG di debug salvato dal parser
        String debugName = System.getProperty("last.debug.filename");
        if (debugName != null) {
            req.setAttribute("debugFileName", debugName);
        }
        System.out.println(
                  " | " + method.name() + " | " + numero_resistenze
        );
        for(int i=0;i<numero_resistenze;i++)
        {
            System.out.println(valori_resistenze.get(i));
        }
        for(int i=0;i<numero_induttanze;i++)
        {
            System.out.println(valori_induttanze.get(i));
        }
        for(int i=0;i<numero_condensatori;i++)
        {
            System.out.println(valori_condensatori.get(i));
        }
        for(int i=0;i<numero_gen_corrente;i++)
        {
            System.out.println(valori_generatori_corrente.get(i));
        }
        for(int i=0;i<numero_gen_tensione;i++)
        {
            System.out.println(valori_generatori_tensione.get(i));
        }
        for(int i=0;i<numero_correnti_maglia;i++)
        {
            System.out.println("Correnti di maglia"+correnti_di_maglia.get(i));
            System.out.println("Direzioni: "+ direzioni_correnti_maglia.get(i));
        }




        RequestDispatcher dispatcher = req.getRequestDispatcher("/result.jsp");
        dispatcher.forward(req, resp);
    }
}

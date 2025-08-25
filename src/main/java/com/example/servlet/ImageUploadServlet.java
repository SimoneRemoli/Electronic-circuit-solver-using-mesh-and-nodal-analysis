package com.example.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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



        CircuitMethod method;
        try {
            method = CircuitMethod.valueOf(Objects.requireNonNull(methodStr));
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Metodo non valido");
            return;
        }







        req.setAttribute("method", method.name());
        // prendi il nome del PNG di debug salvato dal parser
        String debugName = System.getProperty("last.debug.filename");
        if (debugName != null) {
            req.setAttribute("debugFileName", debugName);
        }
        System.out.println(
                  " | " + method.name() + " | "
        );
        RequestDispatcher dispatcher = req.getRequestDispatcher("result.jsp");
        dispatcher.forward(req, resp);
    }
}

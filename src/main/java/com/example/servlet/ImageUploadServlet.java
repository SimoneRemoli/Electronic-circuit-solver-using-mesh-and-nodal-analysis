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

        Part filePart = req.getPart("file"); // richiede multipart-config in web.xml
        String methodStr = req.getParameter("method");
        String notes = req.getParameter("notes");

        if (filePart == null || filePart.getSize() == 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "File immagine mancante");
            return;
        }

        CircuitMethod method;
        try {
            method = CircuitMethod.valueOf(Objects.requireNonNull(methodStr));
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Metodo non valido");
            return;
        }

        // Validazione content-type basilare
        String contentType = filePart.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/png") || contentType.equals("image/jpeg"))) {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    "Sono supportati solo PNG/JPEG");
            return;
        }

        // Nome file sicuro
        String submitted = filePart.getSubmittedFileName();
        String baseName = FilenameUtils.getBaseName(submitted == null ? "img" : submitted);
        String ext = contentType.equals("image/png") ? "png" : "jpg";
        String safeName = baseName.replaceAll("[^a-zA-Z0-9-_]", "_")
                + "-" + System.currentTimeMillis() + "." + ext;

        // Salvataggio su disco
        Path target = uploadDir.resolve(safeName);
        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        // Elaborazione (stub): inserirai qui la tua pipeline di riconoscimento
        CircuitProcessingService service = new CircuitProcessingService();
        CircuitProcessingService.EquationSystem system = service.process(target, method);

        // Passa i dati alla JSP di risultato
        req.setAttribute("uploadedFilePath", target.toAbsolutePath().toString());
        req.setAttribute("uploadedFileName", safeName);
        req.setAttribute("method", method.name());
        req.setAttribute("notes", notes);
        req.setAttribute("equations", system.equations); //equazione pronta

        System.out.println(
                safeName + " | " + method.name() + " | " + Objects.toString(notes, "") +
                        " | " + target.toAbsolutePath() + " | " + system.equations + " | " + notes
        );

        RequestDispatcher dispatcher = req.getRequestDispatcher("result.jsp");
        dispatcher.forward(req, resp);
    }
}

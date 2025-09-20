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

@WebServlet("/AssociaComponentiServlet")

public class AssociaComponentiServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

       int num =  ImageUploadServlet.numero_correnti_maglia;
       int numero_res = ImageUploadServlet.numero_resistenze;
       System.out.println(num);


        for (int i = 0; i < num; i++) {
            String[] selectedR = req.getParameterValues("mesh" + i + "_R"); // es. ["R1","R3"]
            String[] selectedL = req.getParameterValues("mesh" + i + "_L");
            String[] selectedC = req.getParameterValues("mesh" + i + "_C");
            String[] selectedI = req.getParameterValues("mesh" + i + "_I");
            String[] selectedV = req.getParameterValues("mesh" + i + "_V");

            if (selectedR != null) {
                for (String code : selectedR) {
                    System.out.println("Maglia " + i + " componente " + code);
                }
            } else {
                System.out.println("Maglia " + i + " nessuna resistenza selezionata");
            }

            if (selectedL != null) {
                for (String code : selectedL) {
                    System.out.println("Maglia " + i + " componente " + code);
                }
            } else {
                System.out.println("Maglia " + i + " nessuna induttanza selezionata");
            }

            if (selectedC != null) {
                for (String code : selectedC) {
                    System.out.println("Maglia " + i + " componente " + code);
                }
            } else {
                System.out.println("Maglia " + i + " nessun condensatore selezionato");
            }

            if (selectedI != null) {
                for (String code : selectedI) {
                    System.out.println("Maglia " + i + " componente " + code);
                }
            } else {
                System.out.println("Maglia " + i + " nessun generatore di corrente selezionato");
            }

            if (selectedV != null) {
                for (String code : selectedV) {
                    System.out.println("Maglia " + i + " componente " + code);
                }
            } else {
                System.out.println("Maglia " + i + " nessun generatore di tensione selezionato");
            }
        }




    }
}

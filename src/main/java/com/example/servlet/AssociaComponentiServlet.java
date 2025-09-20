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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@WebServlet("/AssociaComponentiServlet")

public class AssociaComponentiServlet extends HttpServlet {

    int num =  ImageUploadServlet.numero_correnti_maglia;
    String[][] m = new String[num][100];
    int j = 0;
    List<String> direzioni_correnti_maglia = ImageUploadServlet.direzioni_correnti_maglia;
    String[][] equazioni = new String[num][100];



    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {



        System.out.println(num);


        for (int i = 0; i < num; i++) {
            j = 0;
            String[] selectedR = req.getParameterValues("mesh" + i + "_R");
            String[] selectedL = req.getParameterValues("mesh" + i + "_L");
            String[] selectedC = req.getParameterValues("mesh" + i + "_C");
            String[] selectedI = req.getParameterValues("mesh" + i + "_I");
            String[] selectedV = req.getParameterValues("mesh" + i + "_V");



            if (selectedR != null) {
                for (String code : selectedR) {
                    System.out.println("Maglia " + i + " componente " + code);
                    m[i][j] = code; j = j + 1;
                }
            }

            if (selectedL != null) {
                for (String code : selectedL) {
                    System.out.println("Maglia " + i + " componente " + code);
                    m[i][j] = code; j = j + 1;
                }
            }

            if (selectedC != null) {
                for (String code : selectedC) {
                    System.out.println("Maglia " + i + " componente " + code);
                    m[i][j] = code; j = j + 1;
                }
            }

            if (selectedI != null) {
                for (String code : selectedI) {
                    System.out.println("Maglia " + i + " componente " + code);
                    m[i][j] = code; j = j + 1;
                }
            }

            if (selectedV != null) {
                for (String code : selectedV) {
                    System.out.println("Maglia " + i + " componente " + code);
                    m[i][j] = code; j = j + 1;
                }
            }
        }

        for (int i = 0; i < m.length; i++) {
            System.out.printf("%02d | ", i); // indice riga
            for (int j = 0; j < m[i].length; j++) {
                String cell = java.util.Objects.toString(m[i][j], ""); // null -> ""
                System.out.print(cell);
                if (j < m[i].length - 1) System.out.print("\t");       // separatore
            }
            System.out.println();
        }

        for(int i=0;i<num;i++)
        {
            System.out.println(direzioni_correnti_maglia.get(i));//CCW antiorario
        }


        //ora da qui genero il sistema
        System.out.println();
        String equazione = null;
        j = 0;

        for (int i = 0; i < m.length; i++) {
            j=0;
            System.out.printf("%02d | ", i); // indice riga
            for (int j = 0; j < m[i].length; j++) {
                String cell = java.util.Objects.toString(m[i][j], ""); // null -> ""
                if(cell.startsWith("L"))
                {
                    equazioni[i][j] += " j w "+cell+ " +";
                }
                if(cell.startsWith("R"))
                {
                    equazioni[i][j] += " "+cell+ " +";
                }
                if(cell.startsWith("C"))
                {
                    equazioni[i][j] += " 1/(j w "+cell+ " ) +";
                }
            }
            System.out.println();
        }









        for (int i = 0; i < equazioni.length; i++) {
            for (int j = 0; j < equazioni[i].length; j++) {
                String cell = java.util.Objects.toString(equazioni[i][j], ""); // null -> ""
                System.out.print(cell);
                if (j < m[i].length - 1) System.out.print("\t");       // separatore
            }
            System.out.println();
        }




    }
}

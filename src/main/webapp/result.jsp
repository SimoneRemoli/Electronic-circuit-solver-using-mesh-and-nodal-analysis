<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>

<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8" />
  <title>Risultato analisi</title>
  <style>
    body { font-family: system-ui, sans-serif; margin: 2rem; }
    .wrap { max-width: 900px; }
    .grid { display: grid; grid-template-columns: 320px 1fr; gap: 1.5rem; align-items: start; }
    img { max-width: 100%; height: auto; border-radius: 10px; border:1px solid #ddd; }
    pre { background: #0d1117; color: #e6edf3; padding: 1rem; border-radius: 10px; overflow:auto; }
    .tag { display:inline-block; padding:.25rem .5rem; background:#eef; border-radius:999px; font-size:.85rem; margin-right:.5rem; }
    .muted { color:#666; }
  </style>
</head>
<body>
<div class="wrap">
  <h1>Risultato analisi</h1>
  <p>
  <%
              String metodo = (String) request.getAttribute("method");
              List<String> valori_resistenze = (List<String>) request.getAttribute("valori_resistenze");
              List<String> valori_induttanze = (List<String>) request.getAttribute("valori_induttanze");
              List<String> valori_condensatori = (List<String>) request.getAttribute("valori_condensatori");
              List<String> valori_generatori_corrente = (List<String>) request.getAttribute("valori_generatori_corrente");
              List<String> valori_generatori_tensione = (List<String>) request.getAttribute("valori_generatori_tensione");
              List<String> correnti_di_maglia = (List<String>) request.getAttribute("correnti_di_maglia");
              List<String> direzioni_correnti_maglia = (List<String>) request.getAttribute("direzioni_correnti_maglia");

  out.println("<span class='tag'>Metodo: "+metodo+"</span>");
  %>

  </p>

  <div class="grid">
    <div>
      <h3>Analisi soluzione</h3>

      <%
        out.print("<style>");
        out.print("table { width: 100%; border-collapse: collapse; font-family: Arial, sans-serif; }");
        out.print("th, td { padding: 12px; border: 1px solid #ddd; text-align: left; }");
        out.print("th { background-color: #0078d7; color: white; font-weight: bold; }");
        out.print("tr:nth-child(even) { background-color: #f9f9f9; }");
        out.print("tr:hover { background-color: #f1f1f1; }");
        out.print("</style>");

        out.print("<table>");
        out.print("<tr><th>Information</th><th>Details</th></tr>");
        // aggiungo qui le mie righe
        out.print("<tr>");
        out.print("<td>Valori Resistenze </td>");
        out.print("<td>");

        if(valori_resistenze.isEmpty())
        {
            out.print(" Nessuna resistenza inserita.");
        }
        else
        {
            for(int i=0;i<valori_resistenze.size();i++)
            {
                            String res = valori_resistenze.get(i);
                            out.print("<span style='color: black; font-weight: bold;'>");
                            out.print("<ul>");
                            out.print("<li>");
                            out.print(" " + res + " ");
                            out.print("</li>");
                            out.print("</ul>");
                            out.print("</span>");
            }
        }
                   out.print("</td>");
                   out.print("</tr>");

                   out.print("<tr>");
                           out.print("<td>Valori Induttanze </td>");
                           out.print("<td>");

                           if(valori_induttanze.isEmpty())
                           {
                               out.print(" Nessuna Induttanza inserita.");
                           }
                           else
                           {
                               for(int i=0;i<valori_induttanze.size();i++)
                               {
                                               String ind = valori_induttanze.get(i);
                                               out.print("<span style='color: black; font-weight: bold;'>");
                                               out.print("<ul>");
                                               out.print("<li>");
                                               out.print(" " + ind + " ");
                                               out.print("</li>");
                                               out.print("</ul>");
                                               out.print("</span>");
                               }
                           }
                                      out.print("</td>");
                                      out.print("</tr>");


                                      out.print("<tr>");
                                              out.print("<td>Valori Condensatori </td>");
                                              out.print("<td>");

                                              if(valori_condensatori.isEmpty())
                                              {
                                                  out.print(" Nessun condensatore inserita.");
                                              }
                                              else
                                              {
                                                  for(int i=0;i<valori_condensatori.size();i++)
                                                  {
                                                                  String con = valori_condensatori.get(i);
                                                                  out.print("<span style='color: black; font-weight: bold;'>");
                                                                  out.print("<ul>");
                                                                  out.print("<li>");
                                                                  out.print(" " + con + " ");
                                                                  out.print("</li>");
                                                                  out.print("</ul>");
                                                                  out.print("</span>");
                                                  }
                                              }
                                                         out.print("</td>");
                                                         out.print("</tr>");

        out.print("</table>");
      %>

    </div>
  </div>




  <p style="margin-top:2rem"><a href="<%= request.getContextPath() %>/index.jsp">↩︎ Torna al caricamento</a></p>
</div>
</body>
</html>

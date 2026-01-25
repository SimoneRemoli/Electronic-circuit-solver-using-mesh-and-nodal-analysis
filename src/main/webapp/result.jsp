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

                Integer numero_resistenze =
                    (Integer) request.getAttribute("valori_resistenze");

                Integer numero_induttanze =
                    (Integer) request.getAttribute("valori_induttanze");

                Integer numero_condensatori =
                    (Integer) request.getAttribute("valori_condensatori");

                Integer numero_generatori_corrente =
                    (Integer) request.getAttribute("valori_generatori_corrente");

                Integer numero_generatori_tensione =
                    (Integer) request.getAttribute("valori_generatori_tensione");

                List<String> correnti_di_maglia =
                    (List<String>) request.getAttribute("correnti_di_maglia");

                List<String> direzioni_correnti_maglia =
                    (List<String>) request.getAttribute("direzioni_correnti_maglia");

  out.println("<span class='tag'>Metodo: "+metodo+"</span>");
  %>

  </p>


 <hr>
 <h2>Associazione componenti alle correnti di maglia</h2>
 <p class="muted">Per ogni corrente di maglia, seleziona i componenti attraversati.</p>

 <form action="AssociaComponentiServlet" method="post">
 <%
   // una sezione per ogni maglia
   for (int i = 0; i < correnti_di_maglia.size(); i++) {
       String nomeMaglia = correnti_di_maglia.get(i);
       String direzione = direzioni_correnti_maglia.get(i);
 %>
   <fieldset style="margin:1rem 0; padding:1rem; border:1px solid #ccc; border-radius:8px;">
     <legend>
       <strong><%= nomeMaglia %></strong> (direzione: <%= direzione %>)
     </legend>

     <!-- RESISTENZE -->
     <label>Resistenze</label><br>
     <%
       if (numero_resistenze == 0) {
     %>
       <em>Nessuna resistenza disponibile</em><br>
     <%
       } else {
         for (int r = 1; r <= numero_resistenze; r++) {
     %>
       <input type="checkbox"
              name="mesh<%=i%>_R"
              value="R<%=r%>"
              id="mesh<%=i%>_R<%=r%>">
       <label for="mesh<%=i%>_R<%=r%>">R<%=r%></label><br>
     <%
         }
       }
     %>
     <br>

     <!-- INDUTTANZE -->
     <label>Induttanze</label><br>
     <%
       if (numero_induttanze == 0) {
     %>
       <em>Nessuna induttanza disponibile</em><br>
     <%
       } else {
         for (int l = 1; l <= numero_induttanze; l++) {
     %>
       <input type="checkbox"
              name="mesh<%=i%>_L"
              value="L<%=l%>"
              id="mesh<%=i%>_L<%=l%>">
       <label for="mesh<%=i%>_L<%=l%>">L<%=l%></label><br>
     <%
         }
       }
     %>
     <br>

     <!-- CONDENSATORI -->
     <label>Condensatori</label><br>
     <%
       if (numero_condensatori == 0) {
     %>
       <em>Nessun condensatore disponibile</em><br>
     <%
       } else {
         for (int c = 1; c <= numero_condensatori; c++) {
     %>
       <input type="checkbox"
              name="mesh<%=i%>_C"
              value="C<%=c%>"
              id="mesh<%=i%>_C<%=c%>">
       <label for="mesh<%=i%>_C<%=c%>">C<%=c%></label><br>
     <%
         }
       }
     %>
     <br>

     <!-- GENERATORI DI CORRENTE -->
     <label>Generatori di corrente</label><br>
     <%
       if (numero_generatori_corrente == 0) {
     %>
       <em>Nessun generatore di corrente disponibile</em><br>
     <%
       } else {
         for (int g = 1; g <= numero_generatori_corrente; g++) {
     %>
       <input type="checkbox"
              name="mesh<%=i%>_I"
              value="I<%=g%>"
              id="mesh<%=i%>_I<%=g%>">
       <label for="mesh<%=i%>_I<%=g%>">I<%=g%></label><br>
     <%
         }
       }
     %>
     <br>

     <!-- GENERATORI DI TENSIONE -->
     <label>Generatori di tensione</label><br>
     <%
       if (numero_generatori_tensione == 0) {
     %>
       <em>Nessun generatore di tensione disponibile</em><br>
     <%
       } else {
         for (int v = 1; v <= numero_generatori_tensione; v++) {
     %>
       <input type="checkbox"
              name="mesh<%=i%>_V"
              value="V<%=v%>"
              id="mesh<%=i%>_V<%=v%>">
       <label for="mesh<%=i%>_V<%=v%>">V<%=v%></label><br>
     <%
         }
       }
     %>
   </fieldset>
 <%
   }
 %>

   <div style="margin-top:1rem;">
     <button type="submit">Conferma associazioni</button>
   </div>
 </form>







  <p style="margin-top:2rem"><a href="<%= request.getContextPath() %>/index.jsp">↩︎ Torna al caricamento</a></p>
</div>
</body>
</html>

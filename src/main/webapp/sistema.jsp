<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8">
  <title>Sistema circuitale — LaTeX</title>
  <script>
    window.MathJax = { tex: { inlineMath: [['$', '$'], ['\\(', '\\)']] } };
  </script>
  <script src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js" defer></script>
  <style>
    body { font-family: system-ui, sans-serif; margin: 2rem; }
    .card { max-width: 1100px; margin: 0 auto; padding: 1rem 1.25rem; border:1px solid #ddd; border-radius: 12px; }
    .muted { color:#666; }
    .grid { display:grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; }
    .field { border:1px solid #e2e2e2; border-radius:10px; padding:.85rem; }
    label { display:block; font-weight:600; margin-bottom:.25rem; }
    input { width:100%; box-sizing:border-box; padding:.45rem .6rem; margin-bottom:.5rem; }
    button { padding:.75rem 1rem; border:0; border-radius:10px; cursor:pointer; }
    .primary { background:#0b5ed7; color:#fff; }
    .error { color:#a40000; }
    .topbar { max-width: 1100px; margin: 0 auto 1rem; display:flex; justify-content:flex-end; }
    .ghost-button {
      padding:.75rem 1rem;
      border-radius:10px;
      border:1px solid #cfd8df;
      background:#fff;
      color:#173042;
      font-weight:600;
      cursor:pointer;
    }
  </style>
</head>
<body>
  <div class="topbar">
    <button class="ghost-button" type="button" onclick="history.back()">Torna indietro</button>
  </div>
  <div class="card">
    <h1>Sistema <%= "NODI".equals(request.getAttribute("method")) ? "dei Nodi" : "delle Maglie" %></h1>
    <% if ("NODI".equals(request.getAttribute("method"))) { %>
      <p class="muted">Nodo di riferimento: <strong><%= request.getAttribute("referenceNodeName") %></strong></p>
    <% } %>

    <h3>Matrice del sistema</h3>
    <div><%= request.getAttribute("latexSystem") %></div>

    <h3>Equazioni della matrice</h3>
    <div><%= request.getAttribute("latexExpandedSystem") %></div>

    <%
      Object lc = request.getAttribute("latexConstraints");
      if (lc != null) {
    %>
      <h3>Relazioni aggiuntive</h3>
      <div><%= lc %></div>
    <%
      }
    %>

    <h3>Sistema completo</h3>
    <div><%= request.getAttribute("latexFullSystem") %></div>

    <%
      Object topologyJson = request.getAttribute("topologyJson");
      if (topologyJson != null && !topologyJson.toString().isBlank()) {
    %>
      <h3>Schema topologico salvato</h3>
      <details>
        <summary>Mostra dati editor visuale</summary>
        <pre style="white-space:pre-wrap; word-break:break-word;"><%= topologyJson %></pre>
      </details>
    <%
      }
    %>

    <hr>
    <h3>Valori numerici</h3>
    <p class="muted">Inserisci \(\omega\) e i valori numerici per risolvere il sistema. Le frazioni scriverle come tale, ad esempio <strong>1/2</strong>.</p>

    <form action="SolveSystemServlet" method="post">
      <div class="grid">
        <div class="field">
          <label for="omega">Omega</label>
          <input type="text" id="omega" name="omega" value="<%= request.getAttribute("omegaValue") != null ? request.getAttribute("omegaValue") : "1" %>">
        </div>

        <%
          List<String> componentSymbols = (List<String>) request.getAttribute("componentSymbols");
          if (componentSymbols != null) {
            for (String code : componentSymbols) {
        %>
          <div class="field">
            <strong><%= code %></strong>
            <%
              if (code.startsWith("R") || code.startsWith("L") || code.startsWith("C")) {
            %>
              <label for="val_<%=code%>">Valore</label>
              <input type="text" id="val_<%=code%>" name="val_<%=code%>" value="<%= request.getParameter("val_" + code) != null ? request.getParameter("val_" + code) : "" %>">
            <%
              } else {
            %>
              <label for="phasor_<%=code%>">Valore fasoriale</label>
              <input type="text" id="phasor_<%=code%>" name="phasor_<%=code%>" placeholder="es. 1/2+3/4j" value="<%= request.getParameter("phasor_" + code) != null ? request.getParameter("phasor_" + code) : "" %>">
            <%
              }
            %>
          </div>
        <%
            }
          }
        %>
      </div>
      <p style="margin-top:1rem; display:flex; gap:.75rem; flex-wrap:wrap;">
        <button class="ghost-button" type="button" onclick="history.back()">Modifica i dati</button>
        <button class="primary" type="submit">Risolvi il sistema</button>
      </p>
    </form>

    <%
      Object solveError = request.getAttribute("solveError");
      if (solveError != null) {
    %>
      <p class="error"><%= solveError %></p>
    <%
      }
      Object numericSystem = request.getAttribute("numericSystemLatex");
      if (numericSystem != null) {
    %>
      <h3>Sistema numerico</h3>
      <div><%= numericSystem %></div>
    <%
      }
      Object numericSolution = request.getAttribute("numericSolutionLatex");
      if (numericSolution != null) {
    %>
      <h3>Soluzione</h3>
      <div><%= numericSolution %></div>
    <%
      }
    %>

    <p style="margin-top:1rem;">
      <button class="ghost-button" type="button" onclick="history.back()">Torna alla pagina precedente</button>
    </p>

    <p class="muted">Impedenze nel dominio \(s\): \(R\), \(j\,\omega L\), \(1/(j\,\omega C)\).</p>
  </div>
</body>
</html>

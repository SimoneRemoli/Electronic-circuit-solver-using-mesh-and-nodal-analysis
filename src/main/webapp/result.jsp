<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
              String notes = (String) request.getAttribute("notes");
              String path = (String) request.getAttribute("uploadedFilePath");

  out.println("<span class='tag'>Metodo: "+metodo+"</span>");
  %>
  <%
        if (notes == null || notes.trim().isEmpty())
        {
            out.println("<span class='tag'>Note: Nessuna nota </span>");
        }
        else
             out.println("<span class='tag'>Note: "+notes+"</span>");
  %>
  </p>

  <div class="grid">
    <div>
      <h3>Immagine caricata</h3>
      <%--
       <img src="${pageContext.request.contextPath}/img/${uploadedFileName}" alt="Circuito" />
      --%>


      <%
      String fileName = (String) request.getAttribute("uploadedFileName");
      String imgUrl = request.getContextPath() + "/img/" + java.net.URLEncoder.encode(fileName, "UTF-8");
      out.println("<img src=\"" + imgUrl + "\" alt='Circuito' style='max-width:100%;height:auto;border:1px solid #ddd;border-radius:8px;'>");
      %>


      <%
out.println("<p style='max-width:100%; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;'>"+ "<small>Percorso immagine: "+path+"</small></p>");
      %>
    </div>

    <div>
      <h3>Sistema di equazioni generato</h3>
      <pre>
<c:forEach var="eq" items="${equations}">${eq}
</c:forEach>
      </pre>
    </div>
  </div>

  <p><a href="${pageContext.request.contextPath}/index.jsp">↩︎ Torna al caricamento</a></p>
</div>
</body>
</html>


<%
Integer nodeCount = (Integer) request.getAttribute("nodeCount");
Integer meshCount = (Integer) request.getAttribute("meshCount");
Integer connectedComponents = (Integer) request.getAttribute("connectedComponents");
java.util.List<com.example.servlet.CircuitComponent> comps =
        (java.util.List<com.example.servlet.CircuitComponent>) request.getAttribute("componentsFound");
java.util.List<java.awt.Point> nodesPx =
        (java.util.List<java.awt.Point>) request.getAttribute("nodesPx");
java.util.List<int[]> branches =
        (java.util.List<int[]>) request.getAttribute("branches");

// safe defaults
if (nodeCount == null) nodeCount = 0;
if (meshCount == null) meshCount = 0;
if (connectedComponents == null) connectedComponents = 0;

// mini escape HTML
java.util.function.Function<String,String> esc = s -> s == null ? "" :
    s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
     .replace("\"","&quot;").replace("'","&#39;");

// --- STATISTICHE ---
out.println("<h3>Risultato parsing</h3>");
out.println("<p><strong>Nodi:</strong> " + nodeCount
          + " &nbsp; <strong>Maglie indipendenti:</strong> " + meshCount
          + " &nbsp; <strong>Componenti connessi:</strong> " + connectedComponents + "</p>");

// --- COMPONENTI ---
if (comps != null && !comps.isEmpty()) {
  out.println("<h4>Componenti riconosciuti (" + comps.size() + ")</h4>");
  out.println("<table border='1' cellpadding='6' style='border-collapse:collapse;'>");
  out.println("<tr><th>Tipo</th><th>Label</th><th>Nodo A</th><th>Nodo B</th></tr>");
  for (com.example.servlet.CircuitComponent c : comps) {
    out.println("<tr><td>" + esc.apply(String.valueOf(c.type)) + "</td>"
                     + "<td>" + esc.apply(c.label) + "</td>"
                     + "<td>" + c.nodeA + "</td>"
                     + "<td>" + c.nodeB + "</td></tr>");
  }
  out.println("</table>");
} else {
  out.println("<p><em>Nessun componente riconosciuto via OCR.</em></p>");
}

// --- NODI (facoltativo: elenco coordinate pixel) ---
if (nodesPx != null && !nodesPx.isEmpty()) {
  out.println("<h4>Nodi (coordinate px)</h4><ul>");
  for (int i=0; i<nodesPx.size(); i++) {
    java.awt.Point p = nodesPx.get(i);
    out.println("<li>N" + i + " = (" + p.x + "," + p.y + ")</li>");
  }
  out.println("</ul>");
}

// --- RAMI (facoltativo) ---
if (branches != null && !branches.isEmpty()) {
  out.println("<h4>Rami/Archi</h4><ul>");
  for (int[] e : branches) {
    out.println("<li>(" + e[0] + " ↔ " + e[1] + ")</li>");
  }
  out.println("</ul>");
}
%>
<%
String dbg = (String) request.getAttribute("debugFileName");
if (dbg != null) {
  String dbgUrl = request.getContextPath() + "/img/" + java.net.URLEncoder.encode(dbg, "UTF-8");
  out.println("<p class='muted'>Overlay di debug (nodi/rami/etichette):</p>");
  out.println("<img src=\"" + dbgUrl + "\" alt='Debug' style='max-width:100%;height:auto;border:1px solid #ddd;border-radius:8px;'>");
} else {
  out.println("<p class='muted'><em>(Nessun overlay di debug disponibile)</em></p>");
}
%>


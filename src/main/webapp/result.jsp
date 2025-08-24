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
  out.println("<span class='tag'>Metodo: "+metodo+"</span>");
  %>
    <c:if test="${not empty notes}">
      <span class="tag">Note: ${fn:escapeXml(notes)}</span>
    </c:if>
  </p>

  <div class="grid">
    <div>
      <h3>Immagine caricata</h3>
      <p class="muted"><em>${uploadedFileName}</em></p>

      <%--
        L'immagine è salvata fuori dal WAR per sicurezza.
        Se vuoi visualizzarla, aggiungi la StaticImageServlet (vedi sotto) e usa un URL tipo:
        <img src="${pageContext.request.contextPath}/img/${uploadedFileName}" alt="Circuito" />
      --%>
      <p><small>Percorso server: ${uploadedFilePath}</small></p>
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

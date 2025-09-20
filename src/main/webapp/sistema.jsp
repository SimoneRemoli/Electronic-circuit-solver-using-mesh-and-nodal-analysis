<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8">
  <title>Sistema delle Maglie — LaTeX</title>
  <script>
    // Config base MathJax
    window.MathJax = { tex: { inlineMath: [['$', '$'], ['\\(', '\\)']] } };
  </script>
  <script src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js" defer></script>
  <style>
    body { font-family: system-ui, sans-serif; margin: 2rem; }
    .card { max-width: 1100px; margin: 0 auto; padding: 1rem 1.25rem; border:1px solid #ddd; border-radius: 12px; }
    .muted { color:#666; }
  </style>
</head>
<body>
  <div class="card">
    <h1>Sistema delle Maglie</h1>
    <div>
      <%= request.getAttribute("latexSystem") %>
    </div>

    <%
      Object lc = request.getAttribute("latexConstraints");
      if (lc != null) {
    %>
      <h3>Vincoli (generatori di corrente)</h3>
      <div><%= lc %></div>
    <%
      }
    %>

    <p class="muted">Impedenze nel dominio \(s\): \(R\), \(j\,\omega L\), \(1/(j\,\omega C)\).</p>
  </div>
</body>
</html>

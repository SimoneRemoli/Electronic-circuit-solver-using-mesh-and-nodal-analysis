<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8" />
  <title>Carica circuito • Maglie/Nodi</title>
  <style>
    body { font-family: system-ui, sans-serif; margin: 2rem; }
    .card { max-width: 680px; padding: 1.25rem; border: 1px solid #ddd; border-radius: 14px; box-shadow: 0 2px 8px rgba(0,0,0,.06); }
    label { display:block; margin: .5rem 0 .25rem; font-weight:600; }
    .row { display:flex; gap:1rem; align-items:center; }
    .row > * { flex:1; }
    button { padding:.7rem 1rem; border-radius:10px; border:0; cursor:pointer; }
    .primary { background:#0b5ed7; color:#fff; }
    .muted { color:#555; font-size:.9rem; }
  </style>
</head>
<body>
  <div class="card">
    <h1>Analisi Circuitale</h1>
    <p class="muted">Scegli il metodo di impostazione del sistema.</p>

<form action="ImageUploadServlet" method="post" enctype="multipart/form-data">
      <label for="method">Metodo di analisi</label>
      <select id="method" name="method" required>
        <option value="MAGLIE">Metodo delle maglie</option>
        <option value="NODI">Metodo dei nodi</option>
      </select>


      <title>Selettore campi dinamici</title>
        <style>
          body { font-family: system-ui, Arial, sans-serif; margin: 2rem; }
          .row { margin: .5rem 0; display: flex; gap: .5rem; align-items: center; }
          label { min-width: 7rem; }
          input[type="text"] { flex: 1; padding: .4rem .6rem; }
          select { padding: .3rem .4rem; }
        </style>
      </head>
      <body>
        <h1>Resistenze</h1>

        <form method="post" action="#">
          <div class="row">
            <label for="count">Quante resistenze?</label>
            <select id="count" name="count" required>
              <option value="" selected disabled>Seleziona…</option>
              <% for (int i = 1; i <= 50; i++) { %>
                <option value="<%= i %>"><%= i %></option>
              <% } %>
            </select>
          </div>

          <div id="inputs"></div>



        <noscript>Attiva JavaScript per generare i campi automaticamente.</noscript>

        <script>
          (function () {
            const select = document.getElementById('count');
            const container = document.getElementById('inputs');

            select.addEventListener('change', function () {
              const n = parseInt(this.value, 10) || 0;

              // Svuota e ricrea
              container.innerHTML = '';
              const frag = document.createDocumentFragment();

              for (let i = 1; i <= n; i++) {
                const row = document.createElement('div');
                row.className = 'row';

                const label = document.createElement('label');
                label.htmlFor = 'field' + i;
                label.textContent = 'Valore della resistenza ' + i;

                const input = document.createElement('input');
                input.type = 'text';
                input.id = 'field' + i;
                input.name = 'field' + i;   // es. field1, field2, ...
                input.placeholder = 'Valore in ohm';

                row.appendChild(label);
                row.appendChild(input);
                frag.appendChild(row);
              }

              container.appendChild(frag);
            });
          })();
        </script>



















        <div style="align-self:end; text-align:right;">
          <button class="primary" type="submit">Elabora</button>
        </div>
      </div>
    </form>
  </div>
</body>
</html>

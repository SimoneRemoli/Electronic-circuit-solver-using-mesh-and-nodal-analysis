<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8" />
  <title>Carica circuito • Maglie/Nodi</title>
  <style>
    body { font-family: system-ui, sans-serif; margin: 2rem; }
    .card { max-width: 900px; padding: 1.25rem; border: 1px solid #ddd; border-radius: 14px; box-shadow: 0 2px 8px rgba(0,0,0,.06); }
    h1 { margin-top: 0; }
    label { display:block; margin: .5rem 0 .25rem; font-weight:600; }
    .row { display:flex; gap:1rem; align-items:center; margin:.5rem 0; }
    .row > * { flex:1; }
    input[type="text"], input[type="number"], select { padding:.4rem .6rem; }
    button { padding:.7rem 1rem; border-radius:10px; border:0; cursor:pointer; }
    .primary { background:#0b5ed7; color:#fff; }
    .muted { color:#555; font-size:.9rem; }
    .grid { display:grid; grid-template-columns: 1fr 1fr; gap:.6rem 1rem; }
    @media (max-width: 680px) { .grid { grid-template-columns: 1fr; } }
  </style>
</head>
<body>
  <div class="card">
    <h1>Analisi Circuitale</h1>
    <p class="muted">Scegli il metodo di impostazione del sistema e specifica i componenti.</p>

    <!-- UNICO FORM -->
    <form action="ImageUploadServlet" method="post" enctype="multipart/form-data">
      <!-- 1) Metodo di analisi -->
      <label for="method">Metodo di analisi</label>
      <select id="method" name="method" required>
        <option value="" selected disabled>Seleziona…</option>
        <option value="MAGLIE">Metodo delle maglie</option>
        <option value="NODI">Metodo dei nodi</option>
      </select>

      <!-- 2) SOLO SE = MAGLIE: numero correnti + nomi -->
      <div id="meshSection" style="display:none; margin-top: .5rem;">
        <div class="row">
          <label for="meshCount">Numero correnti di maglia</label>
          <input type="number" id="meshCount" name="meshCount" min="1" max="50" placeholder="es. 3" />
        </div>
        <p class="muted">Inserisci il numero, poi assegna un nome a ciascuna corrente (es. I1, I2, I3…).</p>
        <div id="meshNamesContainer" class="grid"></div>
      </div>

      <hr style="margin:1rem 0;">

      <!-- 3) Resistenze -->
      <h2>Resistenze</h2>
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

      <!-- 4) Induttanze -->
      <h2>Induttanze</h2>
      <div class="row">
        <label for="count2">Quante induttanze?</label>
        <select id="count2" name="count2" required>
          <option value="" selected disabled>Seleziona…</option>
          <% for (int i = 1; i <= 50; i++) { %>
            <option value="<%= i %>"><%= i %></option>
          <% } %>
        </select>
      </div>
      <div id="inputs2"></div>

      <!-- 5) Condensatori -->
      <h2>Condensatori</h2>
      <div class="row">
        <label for="count3">Quanti condensatori?</label>
        <select id="count3" name="count3" required>
          <option value="" selected disabled>Seleziona…</option>
          <% for (int i = 1; i <= 50; i++) { %>
            <option value="<%= i %>"><%= i %></option>
          <% } %>
        </select>
      </div>
      <div id="inputs3"></div>

      <!-- 6) GeneratorI di Corrente -->
      <h2>Generatori di corrente</h2>
      <div class="row">
        <label for="count4">Quanti generatori di corrente?</label>
        <select id="count4" name="count4" required>
          <option value="" selected disabled>Seleziona…</option>
          <% for (int i = 1; i <= 50; i++) { %>
            <option value="<%= i %>"><%= i %></option>
          <% } %>
        </select>
      </div>
      <div id="inputs4"></div>

      <!-- 7) GeneratorI di Tensione -->
      <h2>Generatori di tensione</h2>
      <div class="row">
        <label for="count5">Quanti generatori di tensione?</label>
        <select id="count5" name="count5" required>
          <option value="" selected disabled>Seleziona…</option>
          <% for (int i = 1; i <= 50; i++) { %>
            <option value="<%= i %>"><%= i %></option>
          <% } %>
        </select>
      </div>
      <div id="inputs5"></div>

      <div style="text-align:right; margin-top:1rem;">
        <button class="primary" type="submit">Elabora</button>
      </div>
    </form>
  </div>

  <noscript>Attiva JavaScript per generare dinamicamente i campi.</noscript>

  <script>
    // --- MOSTRA/NASCONDI SEZIONE MAGLIE ---
    (function () {
      const methodSel = document.getElementById('method');
      const meshSection = document.getElementById('meshSection');
      const meshCount = document.getElementById('meshCount');
      const meshNames = document.getElementById('meshNamesContainer');

      function renderMeshNames(n) {
        meshNames.innerHTML = '';
        n = Math.min(Math.max(parseInt(n || '0', 10), 0), 50);

        for (let i = 1; i <= n; i++) {
          const wrap = document.createElement('div');

          // Nome corrente
          const label = document.createElement('label');
          label.setAttribute('for', 'meshName' + i);
          label.textContent = 'Nome corrente ' + i;

          const input = document.createElement('input');
          input.type = 'text';
          input.id = 'meshName' + i;
          input.name = 'meshNames' + i;  // <--- lasciato come nel tuo codice attuale
          input.placeholder = 'I' + i;

          // Direzione (Orario / Antiorario)
          const dirWrap = document.createElement('div');
          dirWrap.style.display = 'flex';
          dirWrap.style.gap = '1rem';
          dirWrap.style.alignItems = 'center';
          dirWrap.style.marginTop = '.25rem';

          const cwId = 'meshDir' + i + 'CW';
          const ccwId = 'meshDir' + i + 'CCW';

          const cw = document.createElement('input');
          cw.type = 'checkbox';
          cw.id = cwId;
          cw.name = 'meshDir' + i; // UN solo parametro lato server: "CW" o "CCW"
          cw.value = 'CW';

          const cwLbl = document.createElement('label');
          cwLbl.setAttribute('for', cwId);
          cwLbl.textContent = 'Orario';

          const ccw = document.createElement('input');
          ccw.type = 'checkbox';
          ccw.id = ccwId;
          ccw.name = 'meshDir' + i; // stesso name della coppia
          ccw.value = 'CCW';

          const ccwLbl = document.createElement('label');
          ccwLbl.setAttribute('for', ccwId);
          ccwLbl.textContent = 'Antiorario';

          // Mutua esclusione
          cw.addEventListener('change', () => { if (cw.checked) ccw.checked = false; });
          ccw.addEventListener('change', () => { if (ccw.checked) cw.checked = false; });

          dirWrap.appendChild(cw);
          dirWrap.appendChild(cwLbl);
          dirWrap.appendChild(ccw);
          dirWrap.appendChild(ccwLbl);

          wrap.appendChild(label);
          wrap.appendChild(input);
          wrap.appendChild(dirWrap);

          meshNames.appendChild(wrap);
        }
      }

      methodSel.addEventListener('change', () => {
        const isMaglie = methodSel.value === 'MAGLIE';
        meshSection.style.display = isMaglie ? '' : 'none';
        if (!isMaglie) {
          meshCount.value = '';
          meshNames.innerHTML = '';
        }
      });

      meshCount.addEventListener('input', () => renderMeshNames(meshCount.value));
    })();
  </script>


  <!-- RESISTENZE -->
  <script>
    (function () {
      const select = document.getElementById('count');
      const container = document.getElementById('inputs');
      select.addEventListener('change', function () {
        const n = parseInt(this.value, 10) || 0;
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
          input.name = 'field' + i; // es. field1, field2, ...
          input.placeholder = 'Valore in Ohm';
          row.appendChild(label);
          row.appendChild(input);
          frag.appendChild(row);
        }
        container.appendChild(frag);
      });
    })();
  </script>

  <!-- INDUTTANZE -->
  <script>
    (function () {
      const select = document.getElementById('count2');
      const container = document.getElementById('inputs2');
      select.addEventListener('change', function () {
        const n = parseInt(this.value, 10) || 0;
        container.innerHTML = '';
        const frag = document.createDocumentFragment();
        for (let i = 1; i <= n; i++) {
          const row = document.createElement('div');
          row.className = 'row';
          const label = document.createElement('label');
          label.htmlFor = 'field2' + i;
          label.textContent = 'Valore induttanza ' + i;
          const input = document.createElement('input');
          input.type = 'text';
          input.id = 'field2' + i;
          input.name = 'field2' + i;
          input.placeholder = 'Valore in Henry';
          row.appendChild(label);
          row.appendChild(input);
          frag.appendChild(row);
        }
        container.appendChild(frag);
      });
    })();
  </script>

  <!-- CONDENSATORI -->
  <script>
    (function () {
      const select = document.getElementById('count3');
      const container = document.getElementById('inputs3');
      select.addEventListener('change', function () {
        const n = parseInt(this.value, 10) || 0;
        container.innerHTML = '';
        const frag = document.createDocumentFragment();
        for (let i = 1; i <= n; i++) {
          const row = document.createElement('div');
          row.className = 'row';
          const label = document.createElement('label');
          label.htmlFor = 'field3' + i;
          label.textContent = 'Valore del condensatore ' + i;
          const input = document.createElement('input');
          input.type = 'text';
          input.id = 'field3' + i;
          input.name = 'field3' + i;
          input.placeholder = 'Valore in Farad';
          row.appendChild(label);
          row.appendChild(input);
          frag.appendChild(row);
        }
        container.appendChild(frag);
      });
    })();
  </script>

  <!-- GENERATORI DI CORRENTE -->
  <script>
    (function () {
      const select = document.getElementById('count4');
      const container = document.getElementById('inputs4');
      select.addEventListener('change', function () {
        const n = parseInt(this.value, 10) || 0;
        container.innerHTML = '';
        const frag = document.createDocumentFragment();
        for (let i = 1; i <= n; i++) {
          const row = document.createElement('div');
          row.className = 'row';
          const label = document.createElement('label');
          label.htmlFor = 'field4' + i;
          label.textContent = 'Valore del generatore di corrente ' + i;
          const input = document.createElement('input');
          input.type = 'text';
          input.id = 'field4' + i;
          input.name = 'field4' + i;
          input.placeholder = 'Valore (A)';
          row.appendChild(label);
          row.appendChild(input);
          frag.appendChild(row);
        }
        container.appendChild(frag);
      });
    })();
  </script>

  <!-- GENERATORI DI TENSIONE -->
  <script>
    (function () {
      const select = document.getElementById('count5');
      const container = document.getElementById('inputs5');
      select.addEventListener('change', function () {
        const n = parseInt(this.value, 10) || 0;
        container.innerHTML = '';
        const frag = document.createDocumentFragment();
        for (let i = 1; i <= n; i++) {
          const row = document.createElement('div');
          row.className = 'row';
          const label = document.createElement('label');
          label.htmlFor = 'field5' + i;
          label.textContent = 'Valore del generatore di tensione ' + i;
          const input = document.createElement('input');
          input.type = 'text';
          input.id = 'field5' + i;
          input.name = 'field5' + i;
          input.placeholder = 'Valore (V)';
          row.appendChild(label);
          row.appendChild(input);
          frag.appendChild(row);
        }
        container.appendChild(frag);
      });
    })();
  </script>
</body>
</html>

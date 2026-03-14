<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8" />
  <title>Carica circuito • Maglie/Nodi</title>
  <style>
    :root {
      --bg: #08131f;
      --panel: rgba(10, 26, 40, 0.88);
      --panel-strong: #0d2233;
      --line: rgba(137, 172, 198, 0.25);
      --text: #ebf3f8;
      --muted: #93a9bb;
      --accent: #f5b642;
      --accent-2: #54c6eb;
      --danger: #ff7b63;
      --resistor: #f08a4b;
      --inductor: #77d4ff;
      --capacitor: #b6f36a;
      --current: #ff8a80;
      --voltage: #ffd166;
    }

    * { box-sizing: border-box; }
    body {
      margin: 0;
      color: var(--text);
      font-family: "Trebuchet MS", "Segoe UI", sans-serif;
      background:
        radial-gradient(circle at top left, rgba(84, 198, 235, 0.14), transparent 28%),
        radial-gradient(circle at top right, rgba(245, 182, 66, 0.10), transparent 24%),
        linear-gradient(135deg, #06111c 0%, #0b1826 52%, #07111b 100%);
      min-height: 100vh;
    }

    body::before {
      content: "";
      position: fixed;
      inset: 0;
      pointer-events: none;
      background-image:
        linear-gradient(rgba(84, 198, 235, 0.08) 1px, transparent 1px),
        linear-gradient(90deg, rgba(84, 198, 235, 0.08) 1px, transparent 1px);
      background-size: 28px 28px;
      mask-image: linear-gradient(to bottom, rgba(0,0,0,0.55), transparent 90%);
    }

    .shell {
      max-width: 1120px;
      margin: 0 auto;
      padding: 40px 24px 56px;
    }

    .topbar {
      display: flex;
      justify-content: flex-end;
      margin-bottom: 16px;
    }

    .ghost-link {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-height: 46px;
      padding: 0 18px;
      border-radius: 999px;
      border: 1px solid rgba(137, 172, 198, 0.28);
      background: rgba(255,255,255,0.04);
      color: var(--text);
      text-decoration: none;
      font-weight: 700;
      letter-spacing: .02em;
      transition: transform .18s ease, border-color .18s ease, background .18s ease;
    }

    .ghost-link:hover {
      transform: translateY(-1px);
      border-color: rgba(84, 198, 235, 0.42);
      background: rgba(84, 198, 235, 0.08);
    }

    .hero {
      display: grid;
      grid-template-columns: 1.25fr .9fr;
      gap: 24px;
      margin-bottom: 26px;
    }

    .hero-panel,
    .card {
      background: var(--panel);
      border: 1px solid var(--line);
      border-radius: 24px;
      box-shadow: 0 18px 40px rgba(0, 0, 0, 0.28);
      backdrop-filter: blur(10px);
    }

    .hero-panel {
      padding: 28px 30px;
      position: relative;
      overflow: hidden;
    }

    .hero-panel::after {
      content: "";
      position: absolute;
      inset: auto -8% -32% auto;
      width: 260px;
      height: 260px;
      border-radius: 50%;
      background: radial-gradient(circle, rgba(245, 182, 66, 0.25), transparent 70%);
    }

    .eyebrow {
      display: inline-flex;
      align-items: center;
      gap: 10px;
      padding: 8px 14px;
      border-radius: 999px;
      background: rgba(84, 198, 235, 0.08);
      color: var(--accent-2);
      border: 1px solid rgba(84, 198, 235, 0.18);
      font-size: 12px;
      letter-spacing: 0.18em;
      text-transform: uppercase;
    }

    h1 {
      margin: 18px 0 12px;
      font-size: clamp(2.2rem, 5vw, 4.3rem);
      line-height: .96;
      letter-spacing: -0.04em;
    }

    .hero-copy {
      max-width: 640px;
      color: var(--muted);
      font-size: 1rem;
      line-height: 1.65;
      margin: 0;
    }

    .signal-box {
      padding: 22px 24px;
      display: grid;
      gap: 14px;
      align-content: start;
    }

    .signal-title {
      margin: 0;
      font-size: 1.05rem;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: var(--accent);
    }

    .signal-list {
      display: grid;
      gap: 12px;
    }

    .signal {
      padding: 14px 16px;
      border-radius: 18px;
      border: 1px solid var(--line);
      background: rgba(255, 255, 255, 0.02);
    }

    .signal strong {
      display: block;
      font-size: 0.96rem;
      margin-bottom: 4px;
    }

    .signal span {
      color: var(--muted);
      font-size: 0.92rem;
    }

    .card {
      padding: 28px 30px 32px;
    }

    .section-head {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 20px;
      margin-bottom: 20px;
    }

    .section-head h2 {
      margin: 0;
      font-size: 1.3rem;
      letter-spacing: 0.02em;
    }

    .muted {
      color: var(--muted);
      font-size: 0.94rem;
      line-height: 1.55;
    }

    .method-grid,
    .component-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 18px;
    }

    .field,
    .component-card,
    .entity-panel {
      border: 1px solid var(--line);
      border-radius: 20px;
      background: rgba(255, 255, 255, 0.025);
    }

    .field,
    .entity-panel {
      padding: 18px;
    }

    #referenceNodeField {
      margin-top: 14px;
    }

    .component-card {
      padding: 18px 18px 16px;
      position: relative;
      overflow: hidden;
    }

    .component-card::before {
      content: "";
      position: absolute;
      inset: 0 auto 0 0;
      width: 5px;
      background: var(--accent-2);
    }

    .component-card.resistor::before { background: var(--resistor); }
    .component-card.inductor::before { background: var(--inductor); }
    .component-card.capacitor::before { background: var(--capacitor); }
    .component-card.current::before { background: var(--current); }
    .component-card.voltage::before { background: var(--voltage); }

    .component-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
      margin-bottom: 10px;
    }

    .component-title,
    .entity-title {
      margin: 0;
      font-size: 1rem;
      font-weight: 700;
    }

    .chip {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 38px;
      padding: 6px 10px;
      border-radius: 999px;
      border: 1px solid rgba(255,255,255,0.12);
      background: rgba(255,255,255,0.05);
      color: var(--text);
      font-size: 0.82rem;
      font-weight: 700;
    }

    .field label,
    .component-card label,
    .entity-panel label {
      display: block;
      margin-bottom: 8px;
      font-size: 0.9rem;
      font-weight: 700;
      color: #dbe7ef;
    }

    input[type="text"],
    input[type="number"],
    select {
      width: 100%;
      border: 1px solid rgba(150, 188, 215, 0.18);
      border-radius: 14px;
      padding: 13px 14px;
      background: rgba(3, 11, 18, 0.66);
      color: var(--text);
      outline: none;
      transition: border-color .2s ease, box-shadow .2s ease, transform .2s ease;
    }

    input:focus,
    select:focus {
      border-color: rgba(84, 198, 235, 0.75);
      box-shadow: 0 0 0 4px rgba(84, 198, 235, 0.14);
      transform: translateY(-1px);
    }

    .entity-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 16px;
      margin-top: 14px;
    }

    .direction-row {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 10px;
      margin-top: 12px;
    }

    .direction-option {
      position: relative;
    }

    .direction-option input {
      position: absolute;
      opacity: 0;
      pointer-events: none;
    }

    .direction-option label {
      margin: 0;
      padding: 12px 14px;
      border-radius: 14px;
      border: 1px solid rgba(150, 188, 215, 0.18);
      background: rgba(5, 16, 26, 0.76);
      text-align: center;
      cursor: pointer;
      transition: all .18s ease;
    }

    .direction-option input:checked + label {
      border-color: rgba(245, 182, 66, 0.6);
      background: rgba(245, 182, 66, 0.14);
      color: #ffe7b0;
      box-shadow: inset 0 0 0 1px rgba(245, 182, 66, 0.15);
    }

    .actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 28px;
    }

    .primary {
      border: 0;
      border-radius: 999px;
      padding: 15px 24px;
      background: linear-gradient(135deg, var(--accent), #ff8f3d);
      color: #09131d;
      font-weight: 800;
      letter-spacing: 0.03em;
      cursor: pointer;
      box-shadow: 0 14px 26px rgba(245, 182, 66, 0.24);
      transition: transform .18s ease, box-shadow .18s ease;
    }

    .primary:hover {
      transform: translateY(-1px);
      box-shadow: 0 18px 30px rgba(245, 182, 66, 0.32);
    }

    .component-card p {
      margin: 0;
      color: var(--muted);
      font-size: 0.9rem;
    }

    hr {
      border: 0;
      border-top: 1px solid var(--line);
      margin: 24px 0;
    }

    @media (max-width: 900px) {
      .hero { grid-template-columns: 1fr; }
      .method-grid, .component-grid, .entity-grid { grid-template-columns: 1fr; }
    }

    @media (max-width: 640px) {
      .shell { padding: 22px 16px 34px; }
      .card, .hero-panel { padding: 22px 18px; border-radius: 20px; }
      .direction-row { grid-template-columns: 1fr; }
      .actions { justify-content: stretch; }
      .primary { width: 100%; }
    }
  </style>
</head>
<body>
  <div class="shell">
    <div class="topbar">
      <a class="ghost-link" href="<%= request.getContextPath() %>/index.jsp">Nuova configurazione</a>
    </div>
    <section class="hero">
      <div class="hero-panel">
        <span class="eyebrow">Circuit Lab</span>
        <h1>Imposta il circuito come su un banco prova digitale.</h1>
        <p class="hero-copy">
          Definisci il metodo, scegli quante maglie o nodi vuoi analizzare e configura il parco componenti.
          L’interfaccia riprende il linguaggio visivo dell’elettrotecnica: segnali, moduli e sezioni di rete.
        </p>
      </div>
      <aside class="hero-panel signal-box">
        <h2 class="signal-title">Workflow</h2>
        <div class="signal-list">
          <div class="signal">
            <strong>1. Metodo</strong>
            <span>Seleziona maglie o nodi come schema di impostazione.</span>
          </div>
          <div class="signal">
            <strong>2. Topologia</strong>
            <span>Definisci numero di incognite e verso delle correnti di maglia.</span>
          </div>
          <div class="signal">
            <strong>3. Componenti</strong>
            <span>Prepara resistenze, induttanze, condensatori e generatori.</span>
          </div>
        </div>
      </aside>
    </section>

    <section class="card">
      <div class="section-head">
        <div>
          <h2>Configurazione del sistema</h2>
          <p class="muted">Compila i blocchi come se stessi predisponendo una distinta componenti per l’analisi circuitale.</p>
        </div>
      </div>

      <form action="ImageUploadServlet" method="post" enctype="multipart/form-data">
        <div class="method-grid">
          <div class="field">
            <label for="method">Metodo di analisi</label>
            <select id="method" name="method" required>
              <option value="" selected disabled>Seleziona…</option>
              <option value="MAGLIE">Metodo delle maglie</option>
              <option value="NODI">Metodo dei nodi</option>
            </select>
          </div>
        </div>

        <div id="entitySection" style="display:none; margin-top: 20px;">
          <div class="field">
            <label for="entityCount" id="entityCountLabel">Numero elementi del sistema</label>
            <input type="number" id="entityCount" name="entityCount" min="1" max="50" placeholder="es. 3" />
            <p class="muted" id="entityHint" style="margin: 10px 0 0;">Inserisci il numero, poi assegna un nome a ciascuna incognita.</p>
            <div id="referenceNodeField" style="display:none;">
              <label for="referenceNodeName">Nodo di riferimento</label>
              <input type="text" id="referenceNodeName" name="referenceNodeName" placeholder="es. GND" />
            </div>
          </div>
          <div id="entityNamesContainer" class="entity-grid"></div>
        </div>

        <hr>

        <div class="component-grid">
          <article class="component-card resistor">
            <div class="component-top">
              <h3 class="component-title">Resistenze</h3>
              <span class="chip">R</span>
            </div>
            <p>Elementi dissipativi del circuito.</p>
            <label for="count">Quante resistenze?</label>
            <select id="count" name="count">
              <option value="0" selected>0</option>
              <% for (int i = 1; i <= 50; i++) { %>
                <option value="<%= i %>"><%= i %></option>
              <% } %>
            </select>
          </article>

          <article class="component-card inductor">
            <div class="component-top">
              <h3 class="component-title">Induttanze</h3>
              <span class="chip">L</span>
            </div>
            <p>Componenti reattivi induttivi.</p>
            <label for="count2">Quante induttanze?</label>
            <select id="count2" name="count2">
              <option value="0" selected>0</option>
              <% for (int i = 1; i <= 50; i++) { %>
                <option value="<%= i %>"><%= i %></option>
              <% } %>
            </select>
          </article>

          <article class="component-card capacitor">
            <div class="component-top">
              <h3 class="component-title">Condensatori</h3>
              <span class="chip">C</span>
            </div>
            <p>Accumulo di energia elettrica in campo.</p>
            <label for="count3">Quanti condensatori?</label>
            <select id="count3" name="count3">
              <option value="0" selected>0</option>
              <% for (int i = 1; i <= 50; i++) { %>
                <option value="<%= i %>"><%= i %></option>
              <% } %>
            </select>
          </article>

          <article class="component-card current">
            <div class="component-top">
              <h3 class="component-title">Generatori di corrente</h3>
              <span class="chip">Ig</span>
            </div>
            <p>Sorgenti assegnate in forma fasoriale.</p>
            <label for="count4">Quanti generatori di corrente?</label>
            <select id="count4" name="count4">
              <option value="0" selected>0</option>
              <% for (int i = 1; i <= 50; i++) { %>
                <option value="<%= i %>"><%= i %></option>
              <% } %>
            </select>
          </article>

          <article class="component-card voltage">
            <div class="component-top">
              <h3 class="component-title">Generatori di tensione</h3>
              <span class="chip">Vg</span>
            </div>
            <p>Sorgenti di tensione con indice progressivo globale.</p>
            <label for="count5">Quanti generatori di tensione?</label>
            <select id="count5" name="count5">
              <option value="0" selected>0</option>
              <% for (int i = 1; i <= 50; i++) { %>
                <option value="<%= i %>"><%= i %></option>
              <% } %>
            </select>
          </article>
        </div>

        <div class="actions">
          <button class="primary" type="submit">Passa all’associazione dei componenti</button>
        </div>
      </form>
    </section>
  </div>

  <noscript>Attiva JavaScript per generare dinamicamente i campi.</noscript>

  <script>
    (function () {
      const methodSel = document.getElementById('method');
      const entitySection = document.getElementById('entitySection');
      const entityCount = document.getElementById('entityCount');
      const entityNames = document.getElementById('entityNamesContainer');
      const entityCountLabel = document.getElementById('entityCountLabel');
      const entityHint = document.getElementById('entityHint');
      const referenceNodeField = document.getElementById('referenceNodeField');

      function createDirectionOption(id, name, value, text, checked) {
        const wrap = document.createElement('div');
        wrap.className = 'direction-option';

        const input = document.createElement('input');
        input.type = 'checkbox';
        input.id = id;
        input.name = name;
        input.value = value;
        input.checked = checked;

        const label = document.createElement('label');
        label.setAttribute('for', id);
        label.textContent = text;

        wrap.appendChild(input);
        wrap.appendChild(label);
        return { wrap, input };
      }

      function renderEntities(n) {
        entityNames.innerHTML = '';
        n = Math.min(Math.max(parseInt(n || '0', 10), 0), 50);

        const isMaglie = methodSel.value === 'MAGLIE';
        const baseName = isMaglie ? 'I' : 'V';

        for (let i = 1; i <= n; i++) {
          const panel = document.createElement('div');
          panel.className = 'entity-panel';

          const title = document.createElement('h3');
          title.className = 'entity-title';
          title.textContent = isMaglie ? ('Maglia ' + i) : ('Nodo ' + i);

          const label = document.createElement('label');
          label.setAttribute('for', 'entityName' + i);
          label.textContent = isMaglie ? 'Nome corrente di maglia' : 'Nome nodo incognito';

          const input = document.createElement('input');
          input.type = 'text';
          input.id = 'entityName' + i;
          input.name = 'entityNames' + i;
          input.placeholder = baseName + i;

          panel.appendChild(title);
          panel.appendChild(label);
          panel.appendChild(input);

          if (isMaglie) {
            const dirLabel = document.createElement('label');
            dirLabel.textContent = 'Verso di percorrenza';
            dirLabel.style.marginTop = '12px';
            panel.appendChild(dirLabel);

            const dirWrap = document.createElement('div');
            dirWrap.className = 'direction-row';

            const cwId = 'meshDir' + i + 'CW';
            const ccwId = 'meshDir' + i + 'CCW';
            const cw = createDirectionOption(cwId, 'meshDir' + i, 'CW', 'Orario', true);
            const ccw = createDirectionOption(ccwId, 'meshDir' + i, 'CCW', 'Antiorario', false);

            cw.input.addEventListener('change', () => {
              if (cw.input.checked) ccw.input.checked = false;
              if (!cw.input.checked && !ccw.input.checked) cw.input.checked = true;
            });
            ccw.input.addEventListener('change', () => {
              if (ccw.input.checked) cw.input.checked = false;
              if (!cw.input.checked && !ccw.input.checked) ccw.input.checked = true;
            });

            dirWrap.appendChild(cw.wrap);
            dirWrap.appendChild(ccw.wrap);
            panel.appendChild(dirWrap);
          }

          entityNames.appendChild(panel);
        }
      }

      methodSel.addEventListener('change', () => {
        const selectedMethod = methodSel.value;
        const isVisible = selectedMethod === 'MAGLIE' || selectedMethod === 'NODI';
        entitySection.style.display = isVisible ? '' : 'none';

        if (!isVisible) {
          entityCount.value = '';
          entityNames.innerHTML = '';
          return;
        }

        if (selectedMethod === 'MAGLIE') {
          entityCountLabel.textContent = 'Numero correnti di maglia';
          entityHint.textContent = 'Definisci quante correnti di maglia vuoi usare e assegna un nome tecnico a ciascuna.';
          referenceNodeField.style.display = 'none';
        } else {
          entityCountLabel.textContent = 'Numero nodi non di riferimento';
          entityHint.textContent = 'Indica i nodi incogniti del circuito, escluso il riferimento.';
          referenceNodeField.style.display = '';
        }

        renderEntities(entityCount.value);
      });

      entityCount.addEventListener('input', () => renderEntities(entityCount.value));
    })();
  </script>
</body>
</html>

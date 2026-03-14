<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>

<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8" />
  <title>Associazione componenti</title>
  <style>
    :root {
      --bg: #09141d;
      --panel: rgba(8, 23, 35, 0.9);
      --line: rgba(137, 172, 198, 0.24);
      --text: #eef5f9;
      --muted: #95adbe;
      --accent: #f7b844;
      --accent-2: #53c3ea;
      --resistor: #ef8a4a;
      --inductor: #71d6ff;
      --capacitor: #b8f27a;
      --current: #ff8c7a;
      --voltage: #ffd56b;
    }

    * { box-sizing: border-box; }
    body {
      margin: 0;
      min-height: 100vh;
      color: var(--text);
      font-family: "Trebuchet MS", "Segoe UI", sans-serif;
      background:
        radial-gradient(circle at 15% 0%, rgba(83, 195, 234, 0.14), transparent 24%),
        linear-gradient(160deg, #07111a 0%, #0d1b27 48%, #07111b 100%);
    }

    body::before {
      content: "";
      position: fixed;
      inset: 0;
      pointer-events: none;
      background:
        linear-gradient(rgba(255,255,255,0.025) 1px, transparent 1px),
        linear-gradient(90deg, rgba(255,255,255,0.025) 1px, transparent 1px);
      background-size: 30px 30px;
      opacity: 0.45;
    }

    .shell {
      max-width: 1220px;
      margin: 0 auto;
      padding: 34px 22px 56px;
    }

    .topbar {
      display: flex;
      justify-content: flex-end;
      margin-bottom: 16px;
    }

    .ghost-button,
    .back-link {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-height: 46px;
      padding: 0 18px;
      border-radius: 999px;
      border: 1px solid rgba(162, 191, 212, 0.16);
      background: rgba(255,255,255,0.04);
      color: var(--text);
      text-decoration: none;
      font-weight: 700;
      cursor: pointer;
      transition: transform .16s ease, border-color .16s ease, background .16s ease;
    }

    .ghost-button:hover,
    .back-link:hover {
      transform: translateY(-1px);
      border-color: rgba(83, 195, 234, 0.42);
      background: rgba(83, 195, 234, 0.08);
    }

    .banner,
    .mesh-card {
      background: var(--panel);
      border: 1px solid var(--line);
      border-radius: 24px;
      box-shadow: 0 20px 42px rgba(0, 0, 0, 0.24);
      backdrop-filter: blur(10px);
    }

    .banner {
      padding: 24px 28px;
      display: flex;
      justify-content: space-between;
      gap: 18px;
      align-items: end;
      margin-bottom: 24px;
    }

    .tag {
      display:inline-flex;
      align-items:center;
      gap:10px;
      padding:.45rem .8rem;
      background: rgba(83, 195, 234, 0.08);
      border: 1px solid rgba(83, 195, 234, 0.22);
      border-radius:999px;
      font-size:.82rem;
      font-weight: 700;
      color: #aee9fb;
      letter-spacing: .06em;
      text-transform: uppercase;
    }

    h1 {
      margin: 0 0 10px;
      font-size: clamp(2rem, 4vw, 3.4rem);
      letter-spacing: -0.04em;
      line-height: 0.98;
    }

    .muted {
      color: var(--muted);
      line-height: 1.6;
      margin: 0;
    }

    .mesh-grid {
      display: grid;
      gap: 20px;
    }

    .mesh-card {
      padding: 22px;
      position: relative;
      overflow: hidden;
    }

    .mesh-card::after {
      content: "";
      position: absolute;
      inset: auto -30px -30px auto;
      width: 130px;
      height: 130px;
      border-radius: 50%;
      background: radial-gradient(circle, rgba(247, 184, 68, 0.14), transparent 72%);
    }

    .mesh-head {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 16px;
      margin-bottom: 18px;
    }

    .mesh-head h2 {
      margin: 0;
      font-size: 1.25rem;
    }

    .mesh-meta {
      display: inline-flex;
      align-items: center;
      gap: 10px;
      padding: 10px 14px;
      border-radius: 999px;
      background: rgba(255,255,255,0.03);
      border: 1px solid rgba(255,255,255,0.08);
      color: var(--muted);
      font-size: 0.86rem;
    }

    .component-columns {
      display: grid;
      grid-template-columns: repeat(5, minmax(0, 1fr));
      gap: 16px;
    }

    .component-panel {
      border: 1px solid var(--line);
      border-radius: 18px;
      padding: 16px 14px;
      background: rgba(255, 255, 255, 0.025);
    }

    .component-panel.resistor { box-shadow: inset 0 0 0 1px rgba(239, 138, 74, 0.08); }
    .component-panel.inductor { box-shadow: inset 0 0 0 1px rgba(113, 214, 255, 0.08); }
    .component-panel.capacitor { box-shadow: inset 0 0 0 1px rgba(184, 242, 122, 0.08); }
    .component-panel.current { box-shadow: inset 0 0 0 1px rgba(255, 140, 122, 0.08); }
    .component-panel.voltage { box-shadow: inset 0 0 0 1px rgba(255, 213, 107, 0.08); }

    .panel-head {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 10px;
      margin-bottom: 12px;
    }

    .panel-head h3 {
      margin: 0;
      font-size: 0.96rem;
    }

    .panel-chip {
      min-width: 36px;
      height: 36px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      border: 1px solid rgba(255,255,255,0.1);
      font-weight: 800;
      font-size: 0.84rem;
    }

    .resistor .panel-chip { color: var(--resistor); }
    .inductor .panel-chip { color: var(--inductor); }
    .capacitor .panel-chip { color: var(--capacitor); }
    .current .panel-chip { color: var(--current); }
    .voltage .panel-chip { color: var(--voltage); }

    .empty {
      color: var(--muted);
      font-size: 0.88rem;
      font-style: italic;
    }

    .option-list {
      display: grid;
      gap: 8px;
    }

    .option {
      position: relative;
    }

    .option input[type="checkbox"] {
      position: absolute;
      opacity: 0;
      pointer-events: none;
    }

    .option label {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 10px;
      border-radius: 14px;
      padding: 11px 12px;
      border: 1px solid rgba(162, 191, 212, 0.16);
      background: rgba(4, 13, 20, 0.66);
      cursor: pointer;
      transition: transform .16s ease, border-color .16s ease, background .16s ease;
    }

    .option label::after {
      content: "off";
      font-size: 0.74rem;
      color: var(--muted);
      text-transform: uppercase;
      letter-spacing: .08em;
    }

    .option input:checked + label {
      transform: translateY(-1px);
      border-color: rgba(247, 184, 68, 0.55);
      background: rgba(247, 184, 68, 0.12);
      color: #fff0c6;
    }

    .option input:checked + label::after {
      content: "on";
      color: #ffd56b;
    }

    .direction-select {
      margin-top: 8px;
    }

    .direction-select label {
      display: block;
      margin-bottom: 6px;
      color: var(--muted);
      font-size: 0.8rem;
      font-weight: 700;
    }

    .group-builder {
      margin: 0 0 20px;
      padding: 18px;
      border: 1px solid var(--line);
      border-radius: 20px;
      background: rgba(255,255,255,0.025);
    }

    .group-actions {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
      margin-top: 14px;
    }

    .group-card {
      margin-top: 14px;
      padding: 14px;
      border-radius: 16px;
      border: 1px solid rgba(162, 191, 212, 0.16);
      background: rgba(4, 13, 20, 0.55);
    }

    .group-card h3 {
      margin: 0 0 10px;
      font-size: 0.96rem;
    }

    .group-options {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(110px, 1fr));
      gap: 8px;
    }

    select {
      width: 100%;
      border: 1px solid rgba(162, 191, 212, 0.16);
      border-radius: 12px;
      padding: 11px 12px;
      background: rgba(4, 13, 20, 0.7);
      color: var(--text);
    }

    .actions {
      display: flex;
      justify-content: space-between;
      gap: 14px;
      align-items: center;
      margin-top: 26px;
    }

    .primary {
      border: 0;
      border-radius: 999px;
      padding: 15px 24px;
      background: linear-gradient(135deg, var(--accent), #ff9551);
      color: #0a131b;
      font-weight: 800;
      letter-spacing: .03em;
      cursor: pointer;
      box-shadow: 0 14px 30px rgba(247, 184, 68, 0.24);
    }

    @media (max-width: 1080px) {
      .component-columns { grid-template-columns: repeat(2, minmax(0, 1fr)); }
    }

    @media (max-width: 700px) {
      .shell { padding: 20px 14px 40px; }
      .banner, .mesh-card { padding: 18px; border-radius: 20px; }
      .banner { display: grid; }
      .component-columns { grid-template-columns: 1fr; }
      .actions { flex-direction: column; align-items: stretch; }
      .primary { width: 100%; }
    }
  </style>
</head>
<body>
<div class="shell">
  <div class="topbar">
    <button class="ghost-button" type="button" onclick="history.back()">Torna indietro</button>
  </div>
  <%
    String metodo = (String) request.getAttribute("method");
    Integer numeroResistenze = (Integer) request.getAttribute("valori_resistenze");
    Integer numeroInduttanze = (Integer) request.getAttribute("valori_induttanze");
    Integer numeroCondensatori = (Integer) request.getAttribute("valori_condensatori");
    Integer numeroGeneratoriCorrente = (Integer) request.getAttribute("valori_generatori_corrente");
    Integer numeroGeneratoriTensione = (Integer) request.getAttribute("valori_generatori_tensione");
    Integer entityCount = (Integer) request.getAttribute("entityCount");
    List<String> variableNames = (List<String>) request.getAttribute("variableNames");
    List<String> meshDirections = (List<String>) request.getAttribute("meshDirections");
    String referenceNodeName = (String) request.getAttribute("referenceNodeName");
  %>

  <section class="banner">
    <div>
      <span class="tag">Metodo: <%= metodo %></span>
      <h1>Associa i componenti alle <%= "NODI".equals(metodo) ? "incognite di nodo" : "correnti di maglia" %>.</h1>
      <p class="muted">
        <%= "NODI".equals(metodo)
            ? "Componi ogni nodo come una piastra di connessione: marca tutti i rami collegati."
            : "Distribuisci i componenti nelle maglie come in un sinottico di laboratorio, evidenziando le condivisioni." %>
      </p>
    </div>
    <div class="mesh-meta">
      <span><%= entityCount %> sezioni da configurare</span>
      <% if ("NODI".equals(metodo)) { %>
        <span>Nodo di riferimento: <strong><%= referenceNodeName %></strong></span>
      <% } %>
    </div>
  </section>

  <form action="AssociaComponentiServlet" method="post">
    <section class="group-builder">
      <h2 style="margin:0 0 8px; font-size:1.1rem;">Relazioni tra componenti passivi</h2>
      <p class="muted">Usa i pulsanti per dichiarare gruppi di componenti in serie o in parallelo. Seleziona solo i componenti coinvolti nel gruppo.</p>
      <input type="hidden" name="seriesGroupsData" id="seriesGroupsData">
      <input type="hidden" name="parallelGroupsData" id="parallelGroupsData">
      <div class="group-actions">
        <button class="ghost-button" type="button" id="addSeriesGroup">Aggiungi serie</button>
        <button class="ghost-button" type="button" id="addParallelGroup">Aggiungi parallelo</button>
      </div>
      <div id="groupsContainer"></div>
    </section>

    <div class="mesh-grid">
    <%
      for (int i = 0; i < entityCount; i++) {
        String variableName = variableNames.get(i);
        String direction = meshDirections != null && i < meshDirections.size() ? meshDirections.get(i) : null;
    %>
      <section class="mesh-card">
        <div class="mesh-head">
          <div>
            <h2><%= variableName %></h2>
            <p class="muted" style="margin-top:6px;">
              <%= "MAGLIE".equals(metodo) ? "Zona di percorrenza della corrente di maglia." : "Nodo elettrico da cui ricavare l’equazione di bilancio." %>
            </p>
          </div>
          <div class="mesh-meta">
            <% if ("MAGLIE".equals(metodo)) { %>
              Verso <strong><%= direction %></strong>
            <% } else { %>
              Incognita attiva
            <% } %>
          </div>
        </div>

        <div class="component-columns">
          <article class="component-panel resistor">
            <div class="panel-head">
              <h3>Resistenze</h3>
              <span class="panel-chip">R</span>
            </div>
            <%
              if (numeroResistenze == 0) {
            %>
              <div class="empty">Nessuna resistenza disponibile</div>
            <%
              } else {
            %>
              <div class="option-list">
                <% for (int r = 1; r <= numeroResistenze; r++) { %>
                  <div class="option">
                    <input type="checkbox" name="mesh<%=i%>_R" value="R<%=r%>" id="mesh<%=i%>_R<%=r%>">
                    <label for="mesh<%=i%>_R<%=r%>"><span>R<%=r%></span></label>
                  </div>
                <% } %>
              </div>
            <%
              }
            %>
          </article>

          <article class="component-panel inductor">
            <div class="panel-head">
              <h3>Induttanze</h3>
              <span class="panel-chip">L</span>
            </div>
            <%
              if (numeroInduttanze == 0) {
            %>
              <div class="empty">Nessuna induttanza disponibile</div>
            <%
              } else {
            %>
              <div class="option-list">
                <% for (int l = 1; l <= numeroInduttanze; l++) { %>
                  <div class="option">
                    <input type="checkbox" name="mesh<%=i%>_L" value="L<%=l%>" id="mesh<%=i%>_L<%=l%>">
                    <label for="mesh<%=i%>_L<%=l%>"><span>L<%=l%></span></label>
                  </div>
                <% } %>
              </div>
            <%
              }
            %>
          </article>

          <article class="component-panel capacitor">
            <div class="panel-head">
              <h3>Condensatori</h3>
              <span class="panel-chip">C</span>
            </div>
            <%
              if (numeroCondensatori == 0) {
            %>
              <div class="empty">Nessun condensatore disponibile</div>
            <%
              } else {
            %>
              <div class="option-list">
                <% for (int c = 1; c <= numeroCondensatori; c++) { %>
                  <div class="option">
                    <input type="checkbox" name="mesh<%=i%>_C" value="C<%=c%>" id="mesh<%=i%>_C<%=c%>">
                    <label for="mesh<%=i%>_C<%=c%>"><span>C<%=c%></span></label>
                  </div>
                <% } %>
              </div>
            <%
              }
            %>
          </article>

          <article class="component-panel current">
            <div class="panel-head">
              <h3>Generatori di corrente</h3>
              <span class="panel-chip">Ig</span>
            </div>
            <%
              if (numeroGeneratoriCorrente == 0) {
            %>
              <div class="empty">Nessun generatore di corrente disponibile</div>
            <%
              } else {
            %>
              <div class="option-list">
                <% for (int g = 1; g <= numeroGeneratoriCorrente; g++) {
                     String generatorCode = "NODI".equals(metodo) ? "Ig" + (numeroGeneratoriTensione + g) : "Ig" + g; %>
                  <div class="option">
                    <input type="checkbox" name="mesh<%=i%>_I" value="<%=generatorCode%>" id="mesh<%=i%>_I<%=g%>">
                    <label for="mesh<%=i%>_I<%=g%>"><span><%=generatorCode%></span></label>
                  </div>
                  <% if ("MAGLIE".equals(metodo)) { %>
                    <div class="direction-select">
                      <label for="mesh<%=i%>_I_dir_<%=g%>">Verso rispetto a <%= variableName %></label>
                      <select name="mesh<%=i%>_I_dir_<%=g%>" id="mesh<%=i%>_I_dir_<%=g%>">
                        <option value="concorde" selected>Concorde</option>
                        <option value="discorde">Discorde</option>
                      </select>
                    </div>
                  <% } else { %>
                    <div class="direction-select">
                      <label for="mesh<%=i%>_I_dir_<%=g%>">Freccia rispetto a <%= variableName %></label>
                      <select name="mesh<%=i%>_I_dir_<%=g%>" id="mesh<%=i%>_I_dir_<%=g%>">
                        <option value="entrante" selected>Entrante nel nodo</option>
                        <option value="uscente">Uscente dal nodo</option>
                      </select>
                    </div>
                  <% } %>
                <% } %>
              </div>
            <%
              }
            %>
          </article>

          <article class="component-panel voltage">
            <div class="panel-head">
              <h3>Generatori di tensione</h3>
              <span class="panel-chip">Vg</span>
            </div>
            <%
              if (numeroGeneratoriTensione == 0) {
            %>
              <div class="empty">Nessun generatore di tensione disponibile</div>
            <%
              } else {
            %>
              <div class="option-list">
                <% for (int v = 1; v <= numeroGeneratoriTensione; v++) {
                     String generatorCode = "NODI".equals(metodo) ? "Vg" + v : "Vg" + (numeroGeneratoriCorrente + v); %>
                  <div class="option">
                    <input type="checkbox" name="mesh<%=i%>_V" value="<%=generatorCode%>" id="mesh<%=i%>_V<%=v%>">
                    <label for="mesh<%=i%>_V<%=v%>"><span><%=generatorCode%></span></label>
                  </div>
                  <% if ("MAGLIE".equals(metodo)) { %>
                    <div class="direction-select">
                      <label for="mesh<%=i%>_V_dir_<%=v%>">Verso rispetto a <%= variableName %></label>
                      <select name="mesh<%=i%>_V_dir_<%=v%>" id="mesh<%=i%>_V_dir_<%=v%>">
                        <option value="opposto" selected>Corrente verso il + del generatore</option>
                        <option value="concorde">Corrente verso il - del generatore</option>
                      </select>
                    </div>
                  <% } %>
                  <% if ("NODI".equals(metodo)) { %>
                    <div class="direction-select">
                      <label for="mesh<%=i%>_V_dir_<%=v%>">Segno del + rispetto a <%= variableName %></label>
                      <select name="mesh<%=i%>_V_dir_<%=v%>" id="mesh<%=i%>_V_dir_<%=v%>">
                        <option value="concorde" selected>+ rivolto verso il nodo</option>
                        <option value="discorde">+ rivolto in verso opposto</option>
                      </select>
                    </div>
                  <% } %>
                <% } %>
              </div>
            <%
              }
            %>
          </article>
        </div>
      </section>
    <%
      }
    %>
    </div>

    <div class="actions">
      <a class="back-link" href="<%= request.getContextPath() %>/index.jsp">↩ Torna alla configurazione</a>
      <button class="primary" type="submit">Genera il sistema</button>
    </div>
  </form>
</div>
<script>
  (function () {
    const passiveComponents = [
      <% for (int r = 1; r <= numeroResistenze; r++) { %>"R<%=r%>",<% } %>
      <% for (int l = 1; l <= numeroInduttanze; l++) { %>"L<%=l%>",<% } %>
      <% for (int c = 1; c <= numeroCondensatori; c++) { %>"C<%=c%>",<% } %>
    ];
    const groupsContainer = document.getElementById('groupsContainer');
    const seriesInput = document.getElementById('seriesGroupsData');
    const parallelInput = document.getElementById('parallelGroupsData');
    const groups = { series: [], parallel: [] };

    function sync() {
      seriesInput.value = groups.series.map(g => g.join(',')).filter(Boolean).join('|');
      parallelInput.value = groups.parallel.map(g => g.join(',')).filter(Boolean).join('|');
    }

    function renderGroup(kind, index) {
      const card = document.createElement('div');
      card.className = 'group-card';
      const title = document.createElement('h3');
      title.textContent = (kind === 'series' ? 'Serie ' : 'Parallelo ') + (index + 1);
      card.appendChild(title);

      const options = document.createElement('div');
      options.className = 'group-options';
      passiveComponents.forEach((code) => {
        const wrap = document.createElement('div');
        wrap.className = 'option';
        const input = document.createElement('input');
        input.type = 'checkbox';
        input.id = kind + '_' + index + '_' + code;
        input.checked = groups[kind][index].includes(code);
        const label = document.createElement('label');
        label.setAttribute('for', input.id);
        label.innerHTML = '<span>' + code + '</span>';
        input.addEventListener('change', () => {
          if (input.checked) {
            if (!groups[kind][index].includes(code)) groups[kind][index].push(code);
          } else {
            groups[kind][index] = groups[kind][index].filter((x) => x !== code);
          }
          sync();
        });
        wrap.appendChild(input);
        wrap.appendChild(label);
        options.appendChild(wrap);
      });
      card.appendChild(options);
      groupsContainer.appendChild(card);
    }

    document.getElementById('addSeriesGroup').addEventListener('click', () => {
      groups.series.push([]);
      renderGroup('series', groups.series.length - 1);
      sync();
    });

    document.getElementById('addParallelGroup').addEventListener('click', () => {
      groups.parallel.push([]);
      renderGroup('parallel', groups.parallel.length - 1);
      sync();
    });
  })();
</script>
</body>
</html>

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

    .topology-builder {
      margin: 0 0 20px;
      padding: 20px;
      border: 1px solid var(--line);
      border-radius: 20px;
      background: rgba(255,255,255,0.025);
    }

    .topology-layout {
      display: grid;
      grid-template-columns: 1.2fr .9fr;
      gap: 18px;
      margin-top: 16px;
    }

    .topology-canvas {
      position: relative;
      min-height: 460px;
      border-radius: 20px;
      border: 1px solid rgba(162, 191, 212, 0.16);
      background:
        linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
        linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px),
        rgba(4, 13, 20, 0.55);
      background-size: 28px 28px;
      overflow: hidden;
    }

    .topology-canvas svg {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
    }

    .topology-node {
      position: absolute;
      width: 22px;
      height: 22px;
      margin: -11px 0 0 -11px;
      border-radius: 50%;
      border: 2px solid rgba(83, 195, 234, 0.5);
      background: #0d2233;
      box-shadow: 0 0 0 6px rgba(83, 195, 234, 0.10);
      cursor: grab;
    }

    .topology-node-label {
      position: absolute;
      transform: translate(16px, -12px);
      font-size: 0.78rem;
      color: #aee9fb;
      font-weight: 700;
      pointer-events: none;
      white-space: nowrap;
    }

    .topology-marker {
      position: absolute;
      min-width: 42px;
      height: 30px;
      padding: 0 10px;
      margin: -15px 0 0 -21px;
      border-radius: 999px;
      border: 1px solid rgba(247, 184, 68, 0.45);
      background: rgba(247, 184, 68, 0.12);
      color: #fff0c6;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: 0.78rem;
      font-weight: 800;
      cursor: grab;
      box-shadow: 0 0 0 6px rgba(247, 184, 68, 0.08);
    }

    .topology-sidebar {
      display: grid;
      gap: 14px;
      align-content: start;
    }

    .topology-panel {
      padding: 14px;
      border-radius: 18px;
      border: 1px solid rgba(162, 191, 212, 0.16);
      background: rgba(4, 13, 20, 0.55);
    }

    .topology-panel h3 {
      margin: 0 0 10px;
      font-size: 0.96rem;
    }

    .topology-list {
      display: grid;
      gap: 10px;
      max-height: 340px;
      overflow: auto;
    }

    .topology-card {
      padding: 12px;
      border-radius: 14px;
      border: 1px solid rgba(162, 191, 212, 0.16);
      background: rgba(255,255,255,0.025);
    }

    .topology-card strong {
      display: block;
      margin-bottom: 8px;
    }

    .topology-card label {
      display: block;
      margin-bottom: 4px;
      font-size: 0.78rem;
      color: var(--muted);
      font-weight: 700;
    }

    .topology-card input[type="text"] {
      width: 100%;
      border: 1px solid rgba(162, 191, 212, 0.16);
      border-radius: 10px;
      padding: 9px 10px;
      background: rgba(4, 13, 20, 0.7);
      color: var(--text);
      margin-bottom: 8px;
    }

    .topology-meta {
      margin-top: 12px;
      color: var(--muted);
      font-size: 0.86rem;
    }

    .topology-actions {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
      margin-top: 12px;
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
      .topology-layout { grid-template-columns: 1fr; }
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

  <%
    Object topologyValidationError = request.getAttribute("topologyValidationError");
    if (topologyValidationError != null) {
  %>
    <div style="margin: 0 0 18px; padding: 12px 14px; border: 1px solid rgba(255,120,120,0.35); border-radius: 14px; color: #ffd0d0; background: rgba(110, 18, 18, 0.18);">
      <strong>Errore componenti:</strong> <%= topologyValidationError %>
    </div>
  <%
    }
  %>

  <section class="group-builder" style="margin-bottom:18px;">
    <h2 style="margin:0 0 8px; font-size:1.1rem;">Inventario confermato</h2>
    <p class="muted">Elenco completo dei componenti disponibili prima di definire gruppi in serie o in parallelo.</p>
    <div class="mesh-grid">
      <section class="mesh-card">
        <div class="mesh-head">
          <div>
            <h2>Resistenze</h2>
            <p class="muted" style="margin-top:6px;">Componenti passivi resistivi.</p>
          </div>
        </div>
        <p class="muted">
          <%= numeroResistenze != null && numeroResistenze > 0
              ? java.util.stream.IntStream.rangeClosed(1, numeroResistenze).mapToObj(i -> "R" + i).collect(java.util.stream.Collectors.joining(", "))
              : "Non presente" %>
        </p>
      </section>

      <section class="mesh-card">
        <div class="mesh-head">
          <div>
            <h2>Induttanze</h2>
            <p class="muted" style="margin-top:6px;">Componenti passivi induttivi.</p>
          </div>
        </div>
        <p class="muted">
          <%= numeroInduttanze != null && numeroInduttanze > 0
              ? java.util.stream.IntStream.rangeClosed(1, numeroInduttanze).mapToObj(i -> "L" + i).collect(java.util.stream.Collectors.joining(", "))
              : "Non presente" %>
        </p>
      </section>

      <section class="mesh-card">
        <div class="mesh-head">
          <div>
            <h2>Condensatori</h2>
            <p class="muted" style="margin-top:6px;">Componenti passivi capacitivi.</p>
          </div>
        </div>
        <p class="muted">
          <%= numeroCondensatori != null && numeroCondensatori > 0
              ? java.util.stream.IntStream.rangeClosed(1, numeroCondensatori).mapToObj(i -> "C" + i).collect(java.util.stream.Collectors.joining(", "))
              : "Non presente" %>
        </p>
      </section>

      <section class="mesh-card">
        <div class="mesh-head">
          <div>
            <h2>Generatori di corrente</h2>
            <p class="muted" style="margin-top:6px;">Sorgenti di corrente disponibili.</p>
          </div>
        </div>
        <p class="muted">
          <%
            if (numeroGeneratoriCorrente != null && numeroGeneratoriCorrente > 0) {
              java.util.List<String> codes = new java.util.ArrayList<>();
              for (int i = 1; i <= numeroGeneratoriCorrente; i++) {
                codes.add("NODI".equals(metodo) ? "Ig" + (numeroGeneratoriTensione + i) : "Ig" + i);
              }
              out.print(String.join(", ", codes));
            } else {
              out.print("Non presente");
            }
          %>
        </p>
      </section>

      <section class="mesh-card">
        <div class="mesh-head">
          <div>
            <h2>Generatori di tensione</h2>
            <p class="muted" style="margin-top:6px;">Sorgenti di tensione disponibili.</p>
          </div>
        </div>
        <p class="muted">
          <%
            if (numeroGeneratoriTensione != null && numeroGeneratoriTensione > 0) {
              java.util.List<String> codes = new java.util.ArrayList<>();
              for (int i = 1; i <= numeroGeneratoriTensione; i++) {
                codes.add("NODI".equals(metodo) ? "Vg" + i : "Vg" + (numeroGeneratoriCorrente + i));
              }
              out.print(String.join(", ", codes));
            } else {
              out.print("Non presente");
            }
          %>
        </p>
      </section>
    </div>
  </section>

  <form id="analysisForm" action="AssociaComponentiServlet" method="post">
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

    <section class="topology-builder">
      <h2 style="margin:0 0 8px; font-size:1.1rem;">Editor visuale topologico</h2>
      <p class="muted">Costruisci il circuito come grafo: aggiungi nodi, trascinali sul piano e collega i rami. Componenti, generatori e versi vengono letti direttamente da qui per costruire il sistema.</p>
      <input type="hidden" name="topologyData" id="topologyData">
      <input type="hidden" name="topologyNodesData" id="topologyNodesData">
      <input type="hidden" name="topologyBranchesData" id="topologyBranchesData">
      <input type="hidden" name="topologyMeshMarkersData" id="topologyMeshMarkersData">
      <% if ("NODI".equals(metodo)) { %>
        <div class="direction-select" style="max-width:360px; margin-bottom:16px;">
          <label for="referenceTopologyNode">Nodo topologico di riferimento</label>
          <select id="referenceTopologyNode" name="referenceTopologyNode" data-preferred-label="<%= referenceNodeName %>"></select>
        </div>
      <% } %>
      <div class="topology-actions">
        <button class="ghost-button" type="button" id="addTopologyNode">Aggiungi nodo</button>
        <button class="ghost-button" type="button" id="addTopologyBranch">Aggiungi ramo</button>
      </div>
      <div class="topology-layout">
        <div class="topology-canvas" id="topologyCanvas">
          <svg id="topologySvg"></svg>
        </div>
        <div class="topology-sidebar">
          <section class="topology-panel">
            <h3>Nodi</h3>
            <div class="topology-list" id="topologyNodesList"></div>
          </section>
          <section class="topology-panel">
            <h3>Rami</h3>
            <div class="topology-list" id="topologyBranchesList"></div>
            <p class="topology-meta">Ogni ramo collega due nodi e puo contenere uno o piu componenti. Inserisci i codici separati da virgola, ad esempio <strong>R1,Ig1,Vg2</strong>, poi specifica verso della freccia di `Ig` e polo positivo di `Vg` nel ramo.</p>
          </section>
        </div>
      </div>
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

        <% if ("NODI".equals(metodo)) { %>
          <div class="direction-select" style="max-width:360px;">
            <label for="topologyNodeForEntity<%=i%>">Nodo topologico associato</label>
            <select
              id="topologyNodeForEntity<%=i%>"
              name="topologyNodeForEntity<%=i%>"
              class="topology-node-binding"
              data-preferred-label="<%= variableName %>"></select>
          </div>
          <p class="muted" style="margin-top:12px;">
            Tutti i rami incidenti a questo nodo, inclusi generatori e versi, vengono letti dal grafo topologico.
          </p>
        <% } else { %>
          <p class="muted" style="margin-top:12px;">
            Per ogni ramo devi dichiarare esplicitamente quali correnti di maglia lo percorrono e in quale verso. Il sistema viene costruito da queste percorrenze ramo per ramo.
          </p>
        <% } %>
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
    const STORAGE_KEY = 'maglienodi:analysis-draft:v1';
    const form = document.getElementById('analysisForm');
    if (!form) return;

    function saveDraft() {
      const fields = {};
      form.querySelectorAll('[name]').forEach((field) => {
        if (field.type === 'checkbox' || field.type === 'radio') {
          if (field.checked) {
            fields[field.name] = field.value;
          }
        } else {
          fields[field.name] = field.value;
        }
      });
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ fields }));
    }

    function restoreDraft() {
      try {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (!raw) return;
        const saved = JSON.parse(raw);
        const fields = saved && saved.fields ? saved.fields : {};
        Object.keys(fields).forEach((name) => {
          const field = form.elements.namedItem(name);
          if (!field) return;
          if (field instanceof RadioNodeList) {
            Array.from(field).forEach((input) => {
              input.checked = input.value === fields[name];
            });
          } else {
            field.value = fields[name];
          }
        });
      } catch (error) {
        console.warn('Ripristino analisi fallito', error);
      }
    }

    window.__saveAnalysisDraft = saveDraft;
    form.addEventListener('input', saveDraft);
    form.addEventListener('change', saveDraft);
    restoreDraft();
  })();

  (function () {
    const passiveComponents = [
      <% for (int r = 1; r <= numeroResistenze; r++) { %>"R<%=r%>",<% } %>
      <% for (int l = 1; l <= numeroInduttanze; l++) { %>"L<%=l%>",<% } %>
      <% for (int c = 1; c <= numeroCondensatori; c++) { %>"C<%=c%>",<% } %>
    ];
    const sourceComponents = [
      <% if ("NODI".equals(metodo)) { %>
        <% for (int v = 1; v <= numeroGeneratoriTensione; v++) { %>"Vg<%=v%>",<% } %>
        <% for (int g = 1; g <= numeroGeneratoriCorrente; g++) { %>"Ig<%=numeroGeneratoriTensione + g%>",<% } %>
      <% } else { %>
        <% for (int g = 1; g <= numeroGeneratoriCorrente; g++) { %>"Ig<%=g%>",<% } %>
        <% for (int v = 1; v <= numeroGeneratoriTensione; v++) { %>"Vg<%=numeroGeneratoriCorrente + v%>",<% } %>
      <% } %>
    ];
    const allComponents = passiveComponents.concat(sourceComponents);
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

    function restoreGroups(kind, raw) {
      if (!raw) return;
      raw.split('|').filter(Boolean).forEach((group) => {
        groups[kind].push(group.split(',').map((code) => code.trim()).filter(Boolean));
        renderGroup(kind, groups[kind].length - 1);
      });
      sync();
    }

    restoreGroups('series', seriesInput.value);
    restoreGroups('parallel', parallelInput.value);
  })();

  (function () {
    const allComponents = [
      <% for (int r = 1; r <= numeroResistenze; r++) { %>"R<%=r%>",<% } %>
      <% for (int l = 1; l <= numeroInduttanze; l++) { %>"L<%=l%>",<% } %>
      <% for (int c = 1; c <= numeroCondensatori; c++) { %>"C<%=c%>",<% } %>
      <% if ("NODI".equals(metodo)) { %>
        <% for (int v = 1; v <= numeroGeneratoriTensione; v++) { %>"Vg<%=v%>",<% } %>
        <% for (int g = 1; g <= numeroGeneratoriCorrente; g++) { %>"Ig<%=numeroGeneratoriTensione + g%>",<% } %>
      <% } else { %>
        <% for (int g = 1; g <= numeroGeneratoriCorrente; g++) { %>"Ig<%=g%>",<% } %>
        <% for (int v = 1; v <= numeroGeneratoriTensione; v++) { %>"Vg<%=numeroGeneratoriCorrente + v%>",<% } %>
      <% } %>
    ];
    const topologyInput = document.getElementById('topologyData');
    const topologyNodesInput = document.getElementById('topologyNodesData');
    const topologyBranchesInput = document.getElementById('topologyBranchesData');
    const topologyMeshMarkersInput = document.getElementById('topologyMeshMarkersData');
    const canvas = document.getElementById('topologyCanvas');
    const svg = document.getElementById('topologySvg');
    const nodesList = document.getElementById('topologyNodesList');
    const branchesList = document.getElementById('topologyBranchesList');
    const state = { nodes: [], branches: [], markers: [] };
    let dragId = null;
    let dragType = null;

    function defaultNode(index) {
      return {
        id: 'N' + index,
        label: 'Nodo ' + index,
        x: 90 + ((index - 1) % 4) * 120,
        y: 90 + Math.floor((index - 1) / 4) * 110
      };
    }

    function sync() {
      topologyInput.value = JSON.stringify(state);
      topologyNodesInput.value = state.nodes.map((node) => [
        encodeURIComponent(node.id),
        node.x.toFixed(2),
        node.y.toFixed(2),
        encodeURIComponent(node.label || node.id)
      ].join('~')).join('|');
      topologyBranchesInput.value = state.branches.map((branch) => [
        encodeURIComponent(branch.id),
        encodeURIComponent(branch.from),
        encodeURIComponent(branch.to),
        encodeURIComponent(branch.components.join(',')),
        encodeURIComponent(branch.label || branch.id),
        encodeURIComponent(serializeSigns(branch.currentDirections)),
        encodeURIComponent(serializeSigns(branch.voltagePolarities)),
        encodeURIComponent(serializeSigns(branch.meshCurrents)),
        encodeURIComponent(serializeSigns(branch.meshCurrentSourceSigns)),
        encodeURIComponent(serializeSigns(branch.meshVoltageSourceSigns))
      ].join('~')).join('|');
      topologyMeshMarkersInput.value = state.markers.map((marker) => [
        marker.meshIndex,
        marker.x.toFixed(2),
        marker.y.toFixed(2)
      ].join('~')).join('|');
      if (window.__saveAnalysisDraft) {
        window.__saveAnalysisDraft();
      }
    }

    function nodeOptions(selected) {
      return state.nodes.map((node) =>
        '<option value="' + node.id + '"' + (node.id === selected ? ' selected' : '') + '>' + node.label + '</option>'
      ).join('');
    }

    function serializeSigns(signs) {
      return Object.keys(signs || {}).sort().map((code) => code + ':' + signs[code]).join(',');
    }

    function activeSources(branch, prefix) {
      return branch.components.filter((code) => code.indexOf(prefix) === 0);
    }

    function parseComponents(raw) {
      return (raw || '').split(',').map((item) => item.trim()).filter(Boolean);
    }

    function componentInputValue(branch) {
      if (typeof branch.componentsText === 'string') {
        return branch.componentsText;
      }
      return (branch.components || []).join(',');
    }

    function invalidComponents(branch) {
      return parseComponents(componentInputValue(branch)).filter((code) => allComponents.indexOf(code) === -1);
    }

    function pruneSignMaps(branch) {
      const currents = {};
      const voltages = {};
      activeSources(branch, 'Ig').forEach((code) => {
        currents[code] = branch.currentDirections && branch.currentDirections[code] === -1 ? -1 : 1;
      });
      activeSources(branch, 'Vg').forEach((code) => {
        voltages[code] = branch.voltagePolarities && branch.voltagePolarities[code] === -1 ? -1 : 1;
      });
      branch.currentDirections = currents;
      branch.voltagePolarities = voltages;
      branch.meshCurrents = branch.meshCurrents || {};
      branch.meshCurrentSourceSigns = branch.meshCurrentSourceSigns || {};
      branch.meshVoltageSourceSigns = branch.meshVoltageSourceSigns || {};
      if (typeof branch.componentsText !== 'string') {
        branch.componentsText = (branch.components || []).join(',');
      }
    }

    function selectOptions(items, selected, includeBlank) {
      let html = includeBlank ? '<option value="">Seleziona...</option>' : '';
      items.forEach((item) => {
        html += '<option value="' + item.value + '"' + (item.value === selected ? ' selected' : '') + '>' + item.label + '</option>';
      });
      return html;
    }

    function syncTopologyBindings() {
      const bindingSelects = document.querySelectorAll('.topology-node-binding');
      bindingSelects.forEach((select) => {
        const currentValue = state.nodes.some((node) => node.id === select.value) ? select.value : '';
        const preferred = select.dataset.preferredLabel || '';
        let selected = currentValue;
        if (!selected) {
          const preferredNode = state.nodes.find((node) => node.label === preferred);
          selected = preferredNode ? preferredNode.id : '';
        }
        if (!selected && state.nodes.length > 0) {
          selected = state.nodes[0].id;
        }
        select.innerHTML = selectOptions(
          state.nodes.map((node) => ({ value: node.id, label: node.label })),
          selected,
          false
        );
      });

      const referenceSelect = document.getElementById('referenceTopologyNode');
      if (referenceSelect) {
        const currentValue = state.nodes.some((node) => node.id === referenceSelect.value) ? referenceSelect.value : '';
        const preferred = referenceSelect.dataset.preferredLabel || '';
        let selected = currentValue;
        if (!selected) {
          const preferredNode = state.nodes.find((node) => node.label === preferred);
          selected = preferredNode ? preferredNode.id : '';
        }
        if (!selected && state.nodes.length > 0) {
          selected = state.nodes[state.nodes.length - 1].id;
        }
        referenceSelect.innerHTML = selectOptions(
          state.nodes.map((node) => ({ value: node.id, label: node.label })),
          selected,
          false
        );
      }
    }

    function renderNodesList() {
      nodesList.innerHTML = '';
      state.nodes.forEach((node) => {
        const card = document.createElement('div');
        card.className = 'topology-card';
        card.innerHTML =
          '<strong>' + node.id + '</strong>' +
          '<label>Etichetta</label>' +
          '<input type="text" value="' + node.label + '">' +
          '<label>Coordinate</label>' +
          '<div class="muted">x=' + Math.round(node.x) + ' y=' + Math.round(node.y) + '</div>';
        const input = card.querySelector('input');
        input.addEventListener('input', () => {
          node.label = input.value || node.id;
          render();
        });
        nodesList.appendChild(card);
      });
    }

    function renderBranchesList() {
      branchesList.innerHTML = '';
      state.branches.forEach((branch, index) => {
        pruneSignMaps(branch);
        const card = document.createElement('div');
        card.className = 'topology-card';
        const meshList = <% if ("MAGLIE".equals(metodo)) { %>[
          <% for (int i = 0; i < entityCount; i++) { %>
          { code: "<%= variableNames.get(i) %>", label: "<%= variableNames.get(i) %>" }<%= i < entityCount - 1 ? "," : "" %>
          <% } %>
        ]<% } else { %>[]<% } %>;
        const activeMeshes = meshList.filter((mesh) => (branch.meshCurrents[mesh.code] || 0) !== 0);
        const meshControls = <% if ("MAGLIE".equals(metodo)) { %>meshList.map((mesh) =>
          '<label>Percorrenza ' + mesh.label + '</label>' +
          '<select class="branch-mesh-dir" data-code="' + mesh.code + '">' +
          '<option value="0"' + ((branch.meshCurrents[mesh.code] || 0) === 0 ? ' selected' : '') + '>Non passa</option>' +
          '<option value="1"' + ((branch.meshCurrents[mesh.code] || 0) === 1 ? ' selected' : '') + '>' + branch.from + ' → ' + branch.to + '</option>' +
          '<option value="-1"' + ((branch.meshCurrents[mesh.code] || 0) === -1 ? ' selected' : '') + '>' + branch.to + ' → ' + branch.from + '</option>' +
          '</select>'
        ).join('')<% } else { %>''<% } %>;
        const meshCurrentSignControls = activeMeshes.flatMap((mesh) =>
          activeSources(branch, 'Ig').map((code) =>
            '<label>' + mesh.label + ' vs ' + code + '</label>' +
            '<select class="branch-mesh-current-sign" data-mesh="' + mesh.code + '" data-code="' + code + '">' +
            '<option value="1"' + ((branch.meshCurrentSourceSigns[mesh.code + '@' + code] || 1) === 1 ? ' selected' : '') + '>Concorde a ' + code + '</option>' +
            '<option value="-1"' + ((branch.meshCurrentSourceSigns[mesh.code + '@' + code] || 1) === -1 ? ' selected' : '') + '>Discorde a ' + code + '</option>' +
            '</select>'
          )
        ).join('');
        const meshVoltageSignControls = activeMeshes.flatMap((mesh) =>
          activeSources(branch, 'Vg').map((code) =>
            '<label>' + mesh.label + ' vs ' + code + '</label>' +
            '<select class="branch-mesh-voltage-sign" data-mesh="' + mesh.code + '" data-code="' + code + '">' +
            '<option value="-1"' + ((branch.meshVoltageSourceSigns[mesh.code + '@' + code] || -1) === -1 ? ' selected' : '') + '>Entra dal +, esce dal -</option>' +
            '<option value="1"' + ((branch.meshVoltageSourceSigns[mesh.code + '@' + code] || -1) === 1 ? ' selected' : '') + '>Entra dal -, esce dal +</option>' +
            '</select>'
          )
        ).join('');
        const currentControls = activeSources(branch, 'Ig').map((code) =>
          '<label>Freccia ' + code + '</label>' +
          '<select class="branch-current-dir" data-code="' + code + '">' +
          '<option value="1"' + ((branch.currentDirections[code] || 1) === 1 ? ' selected' : '') + '>' + branch.from + ' → ' + branch.to + '</option>' +
          '<option value="-1"' + ((branch.currentDirections[code] || 1) === -1 ? ' selected' : '') + '>' + branch.to + ' → ' + branch.from + '</option>' +
          '</select>'
        ).join('');
        const voltageControls = activeSources(branch, 'Vg').map((code) =>
          '<label>Polo positivo ' + code + '</label>' +
          '<select class="branch-voltage-pol" data-code="' + code + '">' +
          '<option value="1"' + ((branch.voltagePolarities[code] || 1) === 1 ? ' selected' : '') + '>+ su ' + branch.from + '</option>' +
          '<option value="-1"' + ((branch.voltagePolarities[code] || 1) === -1 ? ' selected' : '') + '>+ su ' + branch.to + '</option>' +
          '</select>'
        ).join('');
        card.innerHTML =
          '<strong>' + branch.id + '</strong>' +
          '<label>Nodo iniziale</label>' +
          '<select class="from-node">' + nodeOptions(branch.from) + '</select>' +
          '<label>Nodo finale</label>' +
          '<select class="to-node">' + nodeOptions(branch.to) + '</select>' +
          '<label>Componenti del ramo</label>' +
          '<input type="text" class="branch-components" value="' + componentInputValue(branch) + '" placeholder="R1,L1,Vg2">' +
          '<label>Descrizione</label>' +
          '<input type="text" class="branch-label" value="' + branch.label + '" placeholder="Ramo superiore">' +
          meshControls +
          meshCurrentSignControls +
          meshVoltageSignControls +
          currentControls +
          voltageControls +
          '<button class="ghost-button" type="button">Elimina ramo</button>';

        card.querySelector('.from-node').addEventListener('change', (event) => {
          branch.from = event.target.value;
          render();
        });
        card.querySelector('.to-node').addEventListener('change', (event) => {
          branch.to = event.target.value;
          render();
        });
        card.querySelector('.branch-components').addEventListener('input', (event) => {
          branch.componentsText = event.target.value;
          branch.components = parseComponents(branch.componentsText);
          pruneSignMaps(branch);
          const invalid = invalidComponents(branch);
          event.target.setCustomValidity(invalid.length ? ('Componenti non validi: ' + invalid.join(', ')) : '');
          event.target.title = invalid.length ? ('Componenti non validi: ' + invalid.join(', ')) : '';
          sync();
          draw();
        });
        card.querySelector('.branch-components').addEventListener('blur', () => {
          render();
        });
        const initialInvalid = invalidComponents(branch);
        const componentInput = card.querySelector('.branch-components');
        componentInput.setCustomValidity(initialInvalid.length ? ('Componenti non validi: ' + initialInvalid.join(', ')) : '');
        componentInput.title = initialInvalid.length ? ('Componenti non validi: ' + initialInvalid.join(', ')) : '';
        card.querySelector('.branch-label').addEventListener('input', (event) => {
          branch.label = event.target.value;
          sync();
          draw();
        });
        card.querySelectorAll('.branch-mesh-dir').forEach((select) => {
          select.addEventListener('change', (event) => {
            const code = event.target.dataset.code;
            const sign = parseInt(event.target.value, 10);
            if (sign === 0) {
              delete branch.meshCurrents[code];
            } else {
              branch.meshCurrents[code] = sign;
            }
            sync();
            renderBranchesList();
            draw();
          });
        });
        card.querySelectorAll('.branch-mesh-current-sign').forEach((select) => {
          select.addEventListener('change', (event) => {
            const key = event.target.dataset.mesh + '@' + event.target.dataset.code;
            branch.meshCurrentSourceSigns[key] = event.target.value === '-1' ? -1 : 1;
            sync();
            draw();
          });
        });
        card.querySelectorAll('.branch-mesh-voltage-sign').forEach((select) => {
          select.addEventListener('change', (event) => {
            const key = event.target.dataset.mesh + '@' + event.target.dataset.code;
            branch.meshVoltageSourceSigns[key] = event.target.value === '1' ? 1 : -1;
            sync();
            draw();
          });
        });
        card.querySelectorAll('.branch-current-dir').forEach((select) => {
          select.addEventListener('change', (event) => {
            branch.currentDirections[event.target.dataset.code] = event.target.value === '-1' ? -1 : 1;
            sync();
            draw();
          });
        });
        card.querySelectorAll('.branch-voltage-pol').forEach((select) => {
          select.addEventListener('change', (event) => {
            branch.voltagePolarities[event.target.dataset.code] = event.target.value === '-1' ? -1 : 1;
            sync();
            draw();
          });
        });
        card.querySelector('button').addEventListener('click', () => {
          state.branches.splice(index, 1);
          render();
        });
        branchesList.appendChild(card);
      });
    }

    function draw() {
      svg.innerHTML = '';
      state.branches.forEach((branch) => {
        const from = state.nodes.find((node) => node.id === branch.from);
        const to = state.nodes.find((node) => node.id === branch.to);
        if (!from || !to) return;

        const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
        line.setAttribute('x1', from.x);
        line.setAttribute('y1', from.y);
        line.setAttribute('x2', to.x);
        line.setAttribute('y2', to.y);
        line.setAttribute('stroke', 'rgba(174, 233, 251, 0.9)');
        line.setAttribute('stroke-width', '3');
        svg.appendChild(line);

        const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        text.setAttribute('x', (from.x + to.x) / 2);
        text.setAttribute('y', (from.y + to.y) / 2 - 8);
        text.setAttribute('fill', '#ffd56b');
        text.setAttribute('font-size', '12');
        text.setAttribute('text-anchor', 'middle');
        const meshes = Object.keys(branch.meshCurrents || {}).filter((code) => (branch.meshCurrents[code] || 0) !== 0);
        const meshSummary = meshes.length ? ' [' + meshes.join(', ') + ']' : '';
        text.textContent = (branch.components.join(' + ') || branch.label || branch.id) + meshSummary;
        svg.appendChild(text);
      });

      canvas.querySelectorAll('.topology-node, .topology-node-label').forEach((element) => element.remove());
      canvas.querySelectorAll('.topology-marker').forEach((element) => element.remove());
      state.nodes.forEach((node) => {
        const dot = document.createElement('div');
        dot.className = 'topology-node';
        dot.style.left = node.x + 'px';
        dot.style.top = node.y + 'px';
        dot.dataset.nodeId = node.id;

        const label = document.createElement('div');
        label.className = 'topology-node-label';
        label.style.left = node.x + 'px';
        label.style.top = node.y + 'px';
        label.textContent = node.label;

        dot.addEventListener('pointerdown', () => {
          dragId = node.id;
          dragType = 'node';
          dot.style.cursor = 'grabbing';
        });

        canvas.appendChild(dot);
        canvas.appendChild(label);
      });
      state.markers.forEach((marker) => {
        const chip = document.createElement('div');
        chip.className = 'topology-marker';
        chip.style.left = marker.x + 'px';
        chip.style.top = marker.y + 'px';
        chip.textContent = marker.label;
        chip.addEventListener('pointerdown', () => {
          dragId = marker.meshIndex;
          dragType = 'marker';
          chip.style.cursor = 'grabbing';
        });
        canvas.appendChild(chip);
      });
      sync();
    }

    function render() {
      syncTopologyBindings();
      renderNodesList();
      renderBranchesList();
      draw();
    }

    canvas.addEventListener('pointermove', (event) => {
      if (!dragId) return;
      const rect = canvas.getBoundingClientRect();
      const x = Math.max(20, Math.min(rect.width - 20, event.clientX - rect.left));
      const y = Math.max(20, Math.min(rect.height - 20, event.clientY - rect.top));
      if (dragType === 'node') {
        const node = state.nodes.find((item) => item.id === dragId);
        if (!node) return;
        node.x = x;
        node.y = y;
      } else if (dragType === 'marker') {
        const marker = state.markers.find((item) => item.meshIndex === dragId);
        if (!marker) return;
        marker.x = x;
        marker.y = y;
      }
      draw();
      renderNodesList();
    });

    window.addEventListener('pointerup', () => {
      dragId = null;
      dragType = null;
      canvas.querySelectorAll('.topology-node').forEach((node) => {
        node.style.cursor = 'grab';
      });
      canvas.querySelectorAll('.topology-marker').forEach((marker) => {
        marker.style.cursor = 'grab';
      });
      sync();
    });

    document.getElementById('addTopologyNode').addEventListener('click', () => {
      const index = state.nodes.length + 1;
      state.nodes.push(defaultNode(index));
      if (state.nodes.length === 2 && state.branches.length === 0) {
        state.branches.push({
          id: 'B1',
          from: state.nodes[0].id,
          to: state.nodes[1].id,
          components: [],
          componentsText: '',
          label: 'Ramo 1',
          currentDirections: {},
          voltagePolarities: {},
          meshCurrents: {},
          meshCurrentSourceSigns: {},
          meshVoltageSourceSigns: {}
        });
      }
      render();
    });

    document.getElementById('addTopologyBranch').addEventListener('click', () => {
      if (state.nodes.length < 2) return;
      const index = state.branches.length + 1;
      state.branches.push({
        id: 'B' + index,
        from: state.nodes[0].id,
        to: state.nodes[Math.min(1, state.nodes.length - 1)].id,
        components: [],
        componentsText: '',
        label: 'Ramo ' + index,
        currentDirections: {},
        voltagePolarities: {},
        meshCurrents: {},
        meshCurrentSourceSigns: {},
        meshVoltageSourceSigns: {}
      });
      render();
    });

    if (topologyInput.value) {
      try {
        const savedState = JSON.parse(topologyInput.value);
        if (savedState && Array.isArray(savedState.nodes) && Array.isArray(savedState.branches)) {
          state.nodes = savedState.nodes;
          state.branches = savedState.branches;
          state.markers = Array.isArray(savedState.markers) ? savedState.markers : [];
        }
      } catch (error) {
        console.warn('Ripristino topologia fallito', error);
      }
    }

    if (state.nodes.length === 0) {
      state.nodes.push(defaultNode(1));
      state.nodes.push(defaultNode(2));
      state.nodes[1].x = 320;
      state.nodes[1].y = 160;
    }
    render();
  })();
</script>
</body>
</html>

package com.example.servlet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AnalysisSessionContext {

    private AnalysisSessionContext() {
    }

    static CircuitMethod method;
    static int equationCount;
    static int numeroResistenze;
    static int numeroInduttanze;
    static int numeroCondensatori;
    static int numeroGeneratoriCorrente;
    static int numeroGeneratoriTensione;
    static List<String> variableNames = new ArrayList<>();
    static List<String> meshDirections = new ArrayList<>();
    static String referenceNodeName;
    static String[][] associations = new String[0][0];
    static Map<String, String> branchLabelByEntityComponent = new LinkedHashMap<>();
    static Map<String, Integer> currentSourceOrientationByMesh = new LinkedHashMap<>();
    static Map<String, Integer> voltageSourceOrientationByMesh = new LinkedHashMap<>();
    static Map<String, Integer> currentSourceOrientationByNode = new LinkedHashMap<>();
    static Map<String, Integer> voltageSourceOrientationByNode = new LinkedHashMap<>();
    static List<String> componentSymbols = new ArrayList<>();
    static List<String> extraUnknowns = new ArrayList<>();
    static String latexMatrix;
    static String latexExpandedSystem;
    static String latexAdditionalRelations;
    static String latexFullSystem;
    static String topologyJson;
    static String topologyNodesData;
    static String topologyBranchesData;
    static String topologyMeshMarkersData;

    static void reset() {
        method = null;
        equationCount = 0;
        numeroResistenze = 0;
        numeroInduttanze = 0;
        numeroCondensatori = 0;
        numeroGeneratoriCorrente = 0;
        numeroGeneratoriTensione = 0;
        variableNames = new ArrayList<>();
        meshDirections = new ArrayList<>();
        referenceNodeName = null;
        associations = new String[0][0];
        branchLabelByEntityComponent = new LinkedHashMap<>();
        currentSourceOrientationByMesh = new LinkedHashMap<>();
        voltageSourceOrientationByMesh = new LinkedHashMap<>();
        currentSourceOrientationByNode = new LinkedHashMap<>();
        voltageSourceOrientationByNode = new LinkedHashMap<>();
        componentSymbols = new ArrayList<>();
        extraUnknowns = new ArrayList<>();
        latexMatrix = null;
        latexExpandedSystem = null;
        latexAdditionalRelations = null;
        latexFullSystem = null;
        topologyJson = null;
        topologyNodesData = null;
        topologyBranchesData = null;
        topologyMeshMarkersData = null;
    }
}

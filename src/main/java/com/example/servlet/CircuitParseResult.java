package com.example.servlet;

import java.awt.Point;
import java.util.List;

public class CircuitParseResult {
    public final List<Point> nodesPx;                // coordinate pixel dei nodi
    public final List<int[]> branches;               // archi/rami (nodeA,nodeB) inclusi i fili
    public final List<CircuitComponent> components;  // componenti riconosciuti via OCR
    public final int nodeCount;
    public final int meshCount;                      // M = E - N + C
    public final int connectedComponents;            // C

    public CircuitParseResult(List<Point> nodesPx,
                              List<int[]> branches,
                              List<CircuitComponent> components,
                              int nodeCount,
                              int meshCount,
                              int connectedComponents) {
        this.nodesPx = nodesPx;
        this.branches = branches;
        this.components = components;
        this.nodeCount = nodeCount;
        this.meshCount = meshCount;
        this.connectedComponents = connectedComponents;
    }
}

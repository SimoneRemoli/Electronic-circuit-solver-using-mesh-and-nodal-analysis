package com.example.servlet;

public class CircuitComponent {
    public final ComponentType type;
    public final int nodeA;  // indice in lista nodi
    public final int nodeB;
    public final String label;   // es: R1, C2, Vg, ecc.

    public CircuitComponent(ComponentType type, int nodeA, int nodeB, String label) {
        this.type = type;
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.label = label;
    }
}

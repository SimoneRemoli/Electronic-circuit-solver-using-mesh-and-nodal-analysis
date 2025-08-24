package com.example.servlet;

import java.nio.file.Path;
import java.util.List;

public class CircuitProcessingService {

    public static class EquationSystem {
        public final CircuitMethod method;
        public final List<String> equations; // es. stringhe LaTeX o testo KVL/KCL

        public EquationSystem(CircuitMethod method, List<String> equations) {
            this.method = method;
            this.equations = equations;
        }
    }

    /**
     * Punto d'ingresso della logica di riconoscimento.
     * Integra qui OpenCV/Tesseract/ML per estrarre componenti/nodi/maglie dall'immagine.
     */
    public EquationSystem process(Path imagePath, CircuitMethod method) {
        // TODO: implementare parsing reale dell'immagine (OCR/CV)
        if (method == CircuitMethod.MAGLIE) {
            return new EquationSystem(method, List.of(
                    "KVL₁: -Vg + R1·I1 + R2·(I1 - I2) = 0",
                    "KVL₂: -R2·(I1 - I2) + R3·I2 = 0"
            ));
        } else {
            return new EquationSystem(method, List.of(
                    "KCL@N1: (V1 - V2)/R1 + (V1 - V3)/R2 + I_s = 0",
                    "KCL@N2: (V2 - V1)/R1 + (V2 - V3)/R3 = 0"
            ));
        }
    }
}
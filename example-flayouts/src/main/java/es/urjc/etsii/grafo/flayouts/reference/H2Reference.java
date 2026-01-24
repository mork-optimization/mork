package es.urjc.etsii.grafo.flayouts.reference;

import java.io.IOException;

/**
 * Loads reference results for Heuristic2 as reported in the previous paper
 */
public class H2Reference extends DRFLPReferenceResults {
    public H2Reference() throws IOException {
        super("H2+LP", 3, 4);
    }
}

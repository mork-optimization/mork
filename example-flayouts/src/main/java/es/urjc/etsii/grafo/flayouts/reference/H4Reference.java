package es.urjc.etsii.grafo.flayouts.reference;

import java.io.IOException;

/**
 * Loads reference results for Heuristic4 as reported in the previous paper
 */
public class H4Reference extends DRFLPReferenceResults {
    public H4Reference() throws IOException {
        super("H4+LP", 7, 8);
    }
}

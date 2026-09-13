package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

/** Same legal moves as FLPOptNeighNew, evaluated on demand using affected-pair deltas. */
public class FLPOptNeighFastNew extends FLPOptNeighNew {
    @AutoconfigConstructor
    public FLPOptNeighFastNew() { super(true, true); }
}

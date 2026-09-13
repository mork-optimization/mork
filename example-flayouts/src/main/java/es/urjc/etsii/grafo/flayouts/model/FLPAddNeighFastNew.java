package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

/** Same legal moves as FLPAddNeighNew, evaluated on demand using affected-pair deltas. */
public class FLPAddNeighFastNew extends FLPAddNeighNew {
    @AutoconfigConstructor
    public FLPAddNeighFastNew() { super(true, true); }
}

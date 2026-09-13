package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

/** Same legal moves as FLPRelocateNeighNew, evaluated on demand using affected-pair deltas. */
public class FLPRelocateNeighFastNew extends FLPRelocateNeighNew {
    @AutoconfigConstructor
    public FLPRelocateNeighFastNew() { super(true, true); }
}

package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

/** Same legal moves as FLPSwapNeighNew, evaluated on demand using affected-pair deltas. */
public class FLPSwapNeighFastNew extends FLPSwapNeighNew {
    @AutoconfigConstructor
    public FLPSwapNeighFastNew() { super(true, true); }
}

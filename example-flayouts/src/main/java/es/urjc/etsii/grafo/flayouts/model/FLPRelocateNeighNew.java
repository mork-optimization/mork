package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

/** Complete directed relocation, including row ends, empty rows and two-facility layouts. */
public class FLPRelocateNeighNew extends FLPPreservingNeighNew {
    @AutoconfigConstructor
    public FLPRelocateNeighNew() { this(true); }
    FLPRelocateNeighNew(boolean fast) { super(fast); }
    @Override
    protected FLPNewUtil.MoveSpace<FLPNewMove> space(FLPSolution s) {
        return FLPNewUtil.relocationSpace(s, 1, false, FLPBlockRelocateNeighNew.Scope.ALL, fast);
    }
}

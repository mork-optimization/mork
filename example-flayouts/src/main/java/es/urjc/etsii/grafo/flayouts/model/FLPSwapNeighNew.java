package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

/** Complete swap neighborhood with lazy enumeration and fast deltas by default. */
public class FLPSwapNeighNew extends FLPPreservingNeighNew {
    @AutoconfigConstructor
    public FLPSwapNeighNew() { this(true); }
    FLPSwapNeighNew(boolean fast) { super(fast); }
    @Override
    protected FLPNewUtil.MoveSpace<FLPNewMove> space(FLPSolution s) {
        var positions = FLPNewUtil.positions(s);
        int n = positions.length;
        return new FLPNewUtil.MoveSpace<>((long) n * n, index -> {
            int i = (int) (index / n), j = (int) (index % n);
            if (i >= j) return null;
            var a = positions[i];
            var b = positions[j];
            
            return FLPNewUtil.swap(s, s.rows[a.row()][a.pos()], s.rows[b.row()][b.pos()], fast);
        });
    }
}

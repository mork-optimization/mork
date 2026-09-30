package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

/** Reversal neighborhood with lazy enumeration and fast deltas by default. */
public class FLPOptNeighNew extends FLPPreservingNeighNew {
    @AutoconfigConstructor
    public FLPOptNeighNew() { this(true); }
    FLPOptNeighNew(boolean fast) { super(fast); }
    @Override
    protected FLPNewUtil.MoveSpace<FLPNewMove> space(FLPSolution s) {
        var positions = FLPNewUtil.positions(s);
        int n = positions.length;
        return new FLPNewUtil.MoveSpace<>((long) n * n, index -> {
            int i = (int) (index / n), j = (int) (index % n);
            if (i >= j) return null;
            var a = positions[i];
            var b = positions[j];
            if (a.row() != b.row()) return null;
            return FLPNewUtil.reverse(s, s.rows[a.row()][a.pos()], s.rows[b.row()][b.pos()], fast);
        });
    }
}

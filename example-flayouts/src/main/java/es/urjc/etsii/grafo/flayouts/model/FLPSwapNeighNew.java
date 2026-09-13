package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

/** Corrected/exposed swap variant with eager full-cost evaluation as the comparison baseline. */
public class FLPSwapNeighNew extends FLPPreservingNeighNew {
    @AutoconfigConstructor
    public FLPSwapNeighNew() { this(false, false); }
    protected FLPSwapNeighNew(boolean fast, boolean lazy) { super(fast, lazy); }
    @Override
    protected FLPNewUtil.MoveSpace space(FLPSolution s) {
        var positions = FLPNewUtil.positions(s);
        int n = positions.length;
        return new FLPNewUtil.MoveSpace((long) n * n, index -> {
            int i = (int) (index / n), j = (int) (index % n);
            if (i >= j) return null;
            var a = positions[i];
            var b = positions[j];
            
            return FLPNewUtil.swap(s, s.rows[a.row()][a.pos()], s.rows[b.row()][b.pos()], fast);
        });
    }
}

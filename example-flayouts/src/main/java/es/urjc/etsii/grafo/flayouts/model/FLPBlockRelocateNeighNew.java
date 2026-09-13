package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;

/** Relocate a contiguous block, optionally reversing it, within or between rows. */
public class FLPBlockRelocateNeighNew extends FLPPreservingNeighNew {
    public enum Scope { ALL, WITHIN, BETWEEN }
    private final int length;
    private final boolean reverse;
    private final Scope scope;
    @AutoconfigConstructor
    public FLPBlockRelocateNeighNew(
            @CategoricalParam(strings = {"2", "3"}) int length,
            @CategoricalParam(strings = {"false", "true"}) boolean reverse,
            @CategoricalParam(strings = {"ALL", "WITHIN", "BETWEEN"}) Scope scope) {
        super(true, true);
        if (length != 2 && length != 3) throw new IllegalArgumentException("Block length must be 2 or 3");
        this.length = length;
        this.reverse = reverse;
        this.scope = java.util.Objects.requireNonNull(scope);
    }
    @Override
    protected FLPNewUtil.MoveSpace space(FLPSolution s) { return FLPNewUtil.relocationSpace(s, length, reverse, scope, true); }
    @Override
    public String toString() { return "FLPBlockRelocateNeighNew{length=" + length + ", reverse=" + reverse + ", scope=" + scope + "}"; }
}

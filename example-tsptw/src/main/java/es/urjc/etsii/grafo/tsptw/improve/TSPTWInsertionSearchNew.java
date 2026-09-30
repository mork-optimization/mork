package es.urjc.etsii.grafo.tsptw.improve;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.tsptw.model.TSPTWNewMoveUtil;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;

import java.util.Objects;

/** Full relocation variant of TSPTWInsertionSearch, including the final customer. */
public class TSPTWInsertionSearchNew extends TSPTWCostSearch {
    public enum Selection { FIRST, BEST }

    private final Selection selection;

    @AutoconfigConstructor
    public TSPTWInsertionSearchNew(@CategoricalParam Selection selection) {
        this.selection = Objects.requireNonNull(selection);
    }

    public TSPTWInsertionSearchNew() { this(Selection.FIRST); }

    @Override
    protected boolean improveOnce(TSPTWSolution solution) {
        return TSPTWNewMoveUtil.improveRelocation(solution, 1, selection == Selection.FIRST);
    }

    @Override
    public String toString() { return "TSPTWInsertionSearchNew{selection=" + selection + "}"; }
}

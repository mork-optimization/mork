package es.urjc.etsii.grafo.mreflp.autoconfig;

import es.urjc.etsii.grafo.autoconfig.generator.ExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.generator.TreeContext;
import es.urjc.etsii.grafo.autoconfig.inventory.IInventoryFilter;
import es.urjc.etsii.grafo.mreflp.create.MREFLPReconstructiveNew;
import es.urjc.etsii.grafo.shake.DestroyRebuild;

/** Removes equivalent complete-solution construction paths only in the focused inventory. */
public final class MREFLPFocusedExplorationFilterNew extends ExplorationFilter {
    private final boolean focused;

    public MREFLPFocusedExplorationFilterNew(IInventoryFilter inventoryFilter) {
        this.focused = inventoryFilter instanceof MREFLPFocusedInventoryFilterNew;
    }

    @Override
    public boolean reject(TreeContext context, Class<?> component) {
        return focused && component == MREFLPReconstructiveNew.class
                && context.branch().peek() != DestroyRebuild.class;
    }
}

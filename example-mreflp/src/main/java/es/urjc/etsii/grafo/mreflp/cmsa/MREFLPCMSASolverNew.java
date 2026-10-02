package es.urjc.etsii.grafo.mreflp.cmsa;

import es.urjc.etsii.grafo.algorithms.cmsa.CMSASolver;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.mreflp.create.MREFLPReconstructionUtil;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.util.TimeControl;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Bounded branch and bound over restricted assignments, seeded by capacity matching. */
public final class MREFLPCMSASolverNew extends CMSASolver<MREFLPSolution, MREFLPInstance, MREFLPAssignmentNew> {
    private final int maxNodes;
    @AutoconfigConstructor
    public MREFLPCMSASolverNew(@IntegerParam(min = 1, max = 1_000_000) int maxNodes) {
        if (maxNodes < 1) throw new IllegalArgumentException("Invalid node budget");
        this.maxNodes = maxNodes;
    }

    @Override public MREFLPSolution solve(MREFLPInstance instance, Set<MREFLPAssignmentNew> components, long maxDurationInMillis) {
        if (maxDurationInMillis < 0) throw new IllegalArgumentException("Negative solver budget");
        if (maxDurationInMillis == 0 || TimeControl.isTimeUp()) return null;
        long start = System.nanoTime();
        var search = new Search(instance, MREFLPRestrictedAssignmentUtil.domains(instance, components),
                maxNodes, TimeUnit.MILLISECONDS.toNanos(maxDurationInMillis), start);
        int[] incumbent = MREFLPRestrictedAssignmentUtil.feasible(instance, search.domains, search.order, search::expired);
        if (incumbent == null) return null;
        var feasible = MREFLPReconstructionUtil.fromAssignments(instance, incumbent);
        search.bestAssignments = incumbent;
        search.bestCost = feasible.cost();
        search.visit(0, 0);
        return MREFLPReconstructionUtil.fromAssignments(instance, search.bestAssignments);
    }

    private static final class Search {
        private final MREFLPInstance instance;
        private final int[][] domains;
        private final int[] order, assignments, counts;
        private final int maxNodes;
        private final long budgetNanos, start;
        private int nodes;
        private int[] bestAssignments;
        private long bestCost;

        private Search(MREFLPInstance instance, int[][] domains, int maxNodes, long budgetNanos, long start) {
            this.instance = instance; this.domains = domains;
            this.maxNodes = maxNodes; this.budgetNanos = budgetNanos;
            this.start = start;
            order = new int[instance.n()];
            assignments = new int[instance.n()];
            Arrays.fill(assignments, -1);
            counts = new int[instance.groups()];
            for (int v = 0; v < order.length; v++) order[v] = v;
            // Most constrained facilities first; deterministic ties make bounded runs reproducible.
            for (int i = 1; i < order.length; i++) {
                int v = order[i], j = i;
                while (j > 0 && domains[order[j - 1]].length > domains[v].length) {
                    order[j] = order[j - 1]; j--;
                }
                order[j] = v;
            }
        }

        private boolean expired() { return TimeControl.isTimeUp() || System.nanoTime() - start >= budgetNanos; }

        private void visit(int depth, long cost) {
            if (cost >= bestCost || nodes >= maxNodes || expired()) return;
            nodes++;
            if (depth == order.length) {
                bestCost = cost; bestAssignments = assignments.clone(); return;
            }
            int v = order[depth];
            int[] groups = new int[domains[v].length];
            long[] deltas = new long[groups.length];
            int available = 0;
            for (int g : domains[v]) {
                if (counts[g] == instance.capacity()) continue;
                long delta = 0;
                for (int p = 0; p < depth; p++) {
                    int u = order[p];
                    delta += instance.flow(u, v) * Math.abs(g - assignments[u]);
                }
                int j = available++;
                while (j > 0 && deltas[j - 1] > delta) {
                    deltas[j] = deltas[j - 1]; groups[j] = groups[j - 1]; j--;
                }
                groups[j] = g; deltas[j] = delta;
            }
            for (int j = 0; j < available && nodes < maxNodes && !expired(); j++) {
                assignments[v] = groups[j]; counts[groups[j]]++;
                // Nonnegative flows make the assigned-pair cost an admissible lower bound.
                visit(depth + 1, cost + deltas[j]);
                counts[groups[j]]--; assignments[v] = -1;
            }
        }
    }
}

package es.urjc.etsii.grafo.graphs.mvc;

import es.urjc.etsii.grafo.algorithms.cmsa.CMSASolver;
import es.urjc.etsii.grafo.graphs.model.Edge;
import es.urjc.etsii.grafo.graphs.model.MSTInstance;
import es.urjc.etsii.grafo.graphs.model.MSTSolution;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Exact solver for the CMSA "Solve" step of the Minimum Vertex Cover demo: given a restricted set
 * of candidate vertices, finds the smallest subset of those candidates that covers every edge of
 * the original instance.
 * <p>
 * Mork does not bundle any ILP solver, so instead of delegating to CPLEX/Gurobi/OR-Tools, this
 * implementation performs an exhaustive branch and bound search. This is only tractable because
 * the restricted sub-instance is expected to stay small: {@link es.urjc.etsii.grafo.algorithms.cmsa.CMSA}
 * only keeps a limited number of candidate vertices active at any given time, aging out and
 * dropping the ones that are not selected by this solver. In problems where the sub-instance can
 * grow large, a real MIP/ILP solver would be plugged in instead, implementing the same {@link CMSASolver} contract.
 * <p>
 * Selecting every candidate vertex is always a feasible (if not optimal) solution to the restricted
 * problem, since candidates only ever come from the vertex set of a previously constructed, and
 * therefore already feasible, vertex cover.
 */
public class MVCExactCoverSolver extends CMSASolver<MSTSolution, MSTInstance> {

    @Override
    public MSTSolution solve(MSTInstance instance, Set<Object> restrictedComponents, long maxDurationInMillis) {
        var candidates = new HashSet<Integer>();
        for (var component : restrictedComponents) {
            candidates.add((Integer) component);
        }

        List<Edge> edges = instance.getEdges();
        long deadline = System.nanoTime() + maxDurationInMillis * 1_000_000L;

        // Trivial upper bound: selecting every candidate vertex is always feasible
        BitSet best = new BitSet(instance.v());
        for (int candidate : candidates) {
            best.set(candidate);
        }
        int[] bestSize = {best.cardinality()};

        search(edges, 0, candidates, new BitSet(instance.v()), best, bestSize, deadline);

        MSTSolution solution = new MSTSolution(instance);
        for (int v = best.nextSetBit(0); v >= 0; v = best.nextSetBit(v + 1)) {
            solution.addToCover(v);
        }
        solution.setScoreCover();
        solution.notifyUpdate();
        return solution;
    }

    /**
     * Depth-first branch and bound. At each step, covers the first still-uncovered edge (scanning
     * from {@code scanFrom}, safe because {@code current} only grows on the way down) by trying
     * both of its candidate endpoints.
     *
     * @return true if the search was aborted because the time budget ran out
     */
    private boolean search(List<Edge> edges, int scanFrom, Set<Integer> candidates, BitSet current, BitSet best, int[] bestSize, long deadline) {
        if (System.nanoTime() > deadline) {
            return true;
        }
        if (current.cardinality() >= bestSize[0]) {
            return false; // Cannot possibly improve on the current best from this branch
        }

        int firstUncovered = -1;
        for (int i = scanFrom; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            if (!current.get(edge.from()) && !current.get(edge.to())) {
                firstUncovered = i;
                break;
            }
        }

        if (firstUncovered == -1) {
            // Every edge is covered: current is feasible and strictly better than best (checked above)
            best.clear();
            best.or(current);
            bestSize[0] = current.cardinality();
            return false;
        }

        Edge edge = edges.get(firstUncovered);
        for (int endpoint : new int[]{edge.from(), edge.to()}) {
            if (!candidates.contains(endpoint) || current.get(endpoint)) {
                continue; // Can only select candidate vertices
            }
            current.set(endpoint);
            boolean timeUp = search(edges, firstUncovered + 1, candidates, current, best, bestSize, deadline);
            current.clear(endpoint);
            if (timeUp) {
                return true;
            }
        }
        return false;
    }
}

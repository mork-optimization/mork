package es.urjc.etsii.grafo.solver.improve.ls;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.MoveComparator;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.solver.improve.DefaultMoveComparator;
import es.urjc.etsii.grafo.solver.improve.IteratedImprover;

import java.util.Arrays;
import java.util.Optional;

/**
 * Local search procedures start from a given feasible solution and explore a determined neighborhood
 * in each iteration, replacing the current solution if a neighbor solution improves the objective
 * function of the current one. The search ends when all neighbor solutions are worse  meaning that a local optimum
 * is found.
 * <p>
 * There exist two typical strategies to explore the corresponding neighborhood:
 * best improvement and first improvement. To use or extend those procedures see {@see es.urjc.etsii.grafo.solver.improve.ls.LocalSearchBestImprovement}
 * and {@see es.urjc.etsii.grafo.solver.improve.ls.LocalSearchFirstImprovement} respectively.
 *
 * @param <M> type of move
 * @param <S> type of the problem solution
 * @param <I> type of the problem instance
 */
public abstract class LocalSearch<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> extends IteratedImprover<S, I> {
    protected final Neighborhood<M, S, I>[] providers;
    protected final MoveComparator<M, S, I> comparator;
    protected final String lsName;

    /**
     * Build a new local search
     *
     * @param comparator comparator to determine between two solutions which one is better
     * @param lsName user defined name for the local search procedure
     * @param ps neighborhood that generates the movements
     */
    @SafeVarargs
    public LocalSearch(MoveComparator<M, S, I> comparator, String lsName, Neighborhood<M, S, I>... ps) {
        this.comparator = comparator;
        this.providers = ps;
        this.lsName = lsName.strip();
    }

    /**
     * Build a new local search
     *
     * @param comparator comparator to determine between two solutions which one is better
     * @param ps neighborhood that generates the movements
     */
    @SafeVarargs
    public LocalSearch(MoveComparator<M, S, I> comparator, Neighborhood<M, S, I>... ps) {
        this(comparator, "", ps);
    }

    /**
     * Build a new local search
     *
     * @param lsName     Local Search name. If present, toString works as name{}. If not, Classname{neigh=[neigborhoods],comp=comparator}
     * @param maximizing true if a movement with a bigger score is better
     * @param ps         neighborhood that generates the movements
     */
    @SafeVarargs
    public LocalSearch(boolean maximizing, String lsName, Neighborhood<M, S, I>... ps) {
        this(new DefaultMoveComparator<>(maximizing), lsName, ps);
    }

    /**
     * Build a new local search
     *
     * @param maximizing true if a movement with a bigger score is better
     * @param ps         neighborhood that generates the movements
     */
    @SafeVarargs
    public LocalSearch(boolean maximizing, Neighborhood<M, S, I>... ps) {
        this(new DefaultMoveComparator<>(maximizing), "", ps);
    }

    /**
     * {@inheritDoc}
     *
     * This procedure check if there are valid moves to neighbors solutions.
     * In that case, the move is executed. Otherwise, the procedure ends.
     */
    @Override
    public boolean iteration(S s) {
        // Get next move to execute
        var move = getMove(s);

        if (move.isEmpty()) {
            return false; // There are no valid transactions, the procedure ends
        }

        // The move is executed and ask for another iteration
        move.get().execute();
        return true;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        if (this.lsName.isEmpty()) {
            return (this.getClass().getSimpleName() + "{" +
                    "neig=" + Arrays.toString(providers) +
                    ", comp=" + comparator +
                    '}').replace("LocalSearch", "LS").replace("Improvement", "");
        } else {
            return this.lsName;
        }
    }

    /**
     * Get next move to execute, different strategies are possible
     *
     * @param s Solution
     * @return Proposed move
     */
    public abstract Optional<M> getMove(S s);

}

package es.urjc.etsii.grafo.improve.ls;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.RefreshableMove;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.ListExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.Context;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.PriorityQueue;

/**
 * Cached best improvement local search.
 * <p>
 * When the cache is empty, this strategy explores the full neighborhood and stores the best {@code cacheSize}
 * improving moves. It executes the best move immediately. In later iterations, it refreshes cached candidates
 * one by one and executes the first refreshed candidate that still improves. If no cached candidate improves,
 * it explores the full neighborhood again.
 * <p>
 * This is a heuristic variant of best improvement: cached candidates are ordered by their score when originally
 * explored, and may not be the best moves after the solution changes.
 *
 * @param <M> the type of move
 * @param <S> the type of problem solution
 * @param <I> the type of problem instances
 */
public class LocalSearchCachedBestImprovement<
        M extends Move<S, I> & RefreshableMove<M, S, I>,
        S extends Solution<S, I>,
        I extends Instance
        > extends LocalSearch<M, S, I> {

    private final int cacheSize;
    private final Deque<M> cachedMoves;

    /**
     * Create a new cached best improvement local search method using the given neighborhood.
     * Uses the method Move::getValue as the guiding function, with fMaximize = fmode.
     *
     * @param neighborhood neighborhood to use
     * @param cacheSize number of best moves to keep after each full neighborhood exploration
     */
    @AutoconfigConstructor
    public LocalSearchCachedBestImprovement(
            Neighborhood<M, S, I> neighborhood,
            @IntegerParam(min = 1, max = 1_000_000) int cacheSize
    ) {
        this(Context.getMainObjective(), neighborhood, cacheSize);
    }

    /**
     * Create a new cached best improvement local search method using the given neighborhood.
     *
     * @param objective objective function to optimize
     * @param neighborhood neighborhood to use
     * @param cacheSize number of best moves to keep after each full neighborhood exploration
     */
    public LocalSearchCachedBestImprovement(
            Objective<M, S, I> objective,
            Neighborhood<M, S, I> neighborhood,
            int cacheSize
    ) {
        super(objective, neighborhood);
        validateCacheSize(cacheSize);
        this.cacheSize = cacheSize;
        this.cachedMoves = new ArrayDeque<>(cacheSize);
    }

    @Override
    public S improve(S solution) {
        this.cachedMoves.clear();
        return super.improve(solution);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Get next move to execute.
     */
    @Override
    public M getMove(S solution) {
        var cachedMove = getRefreshedCachedMove(solution);
        if (cachedMove != null) {
            return cachedMove;
        }

        var topMoves = findTopImprovingMoves(solution);
        if (topMoves.isEmpty()) {
            return null;
        }

        var bestMove = topMoves.removeFirst();
        this.cachedMoves.addAll(topMoves);
        return bestMove;
    }

    private M getRefreshedCachedMove(S solution) {
        while (!this.cachedMoves.isEmpty()) {
            var refreshed = this.cachedMoves.removeFirst().refresh(solution);
            if (refreshed.isEmpty()) {
                continue;
            }

            var move = refreshed.get();
            if (improves(move)) {
                return move;
            }
        }

        return null;
    }

    private Deque<M> findTopImprovingMoves(S solution) {
        // Keep the worst retained candidate at the heap head so addIfTop can evict it in O(log cacheSize).
        // The heap still represents the best cacheSize moves found so far; only its head is ordered worst-first.
        var topMoves = new PriorityQueue<>(this.cacheSize, this::compareWorstFirst);
        var expRes = neighborhood.explore(solution);

        if (expRes instanceof ListExploreResult<M, S, I> list) {
            for (var move : list.moveList()) {
                addIfTop(topMoves, move);
            }
        } else {
            try (var stream = expRes.moves()) {
                stream.forEach(move -> addIfTop(topMoves, move));
            }
        }

        return toBestFirstMoveQueue(topMoves);
    }

    private void addIfTop(PriorityQueue<Candidate<M>> topMoves, M move) {
        var score = this.objective.evalMove(move);
        if (!this.objective.improves(score)) {
            return;
        }

        var candidate = new Candidate<>(move, score);
        if (topMoves.size() < this.cacheSize) {
            topMoves.add(candidate);
        } else if (this.objective.isBetter(score, topMoves.peek().score())) {
            // peek() is the worst retained candidate by design; replace it when a better move is found.
            topMoves.poll();
            topMoves.add(candidate);
        }
    }

    private Deque<M> toBestFirstMoveQueue(PriorityQueue<Candidate<M>> topMoves) {
        var moves = new ArrayDeque<M>(topMoves.size());
        while (!topMoves.isEmpty()) {
            // Polling the heap returns candidates from worst to best; addFirst reverses them for cached execution.
            moves.addFirst(topMoves.poll().move());
        }
        return moves;
    }

    private int compareWorstFirst(Candidate<M> a, Candidate<M> b) {
        if (this.objective.isBetter(a.score(), b.score())) {
            return 1;
        }
        if (this.objective.isBetter(b.score(), a.score())) {
            return -1;
        }
        return 0;
    }

    private void validateCacheSize(int cacheSize) {
        if (cacheSize < 1) {
            throw new IllegalArgumentException("cacheSize must be greater than 0");
        }
    }

    private record Candidate<M>(M move, double score) {}
}

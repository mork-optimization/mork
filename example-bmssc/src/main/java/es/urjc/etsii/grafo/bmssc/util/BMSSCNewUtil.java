package es.urjc.etsii.grafo.bmssc.util;

import es.urjc.etsii.grafo.bmssc.model.sol.AssignMove;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCMove;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

/** Helpers for the additional components; the original BMSSC implementation is left unchanged. */
public final class BMSSCNewUtil {
    public enum SeedStrategy { FARTHEST_FIRST, RANDOM }

    private BMSSCNewUtil() {}

    public static void validateUnitInterval(double value) {
        if (!Double.isFinite(value) || value < 0 || value > 1) {
            throw new IllegalArgumentException("Expected a finite value in [0,1]: " + value);
        }
    }

    public static void requireEmpty(BMSSCSolution solution) {
        if (solution.getNotAssignedPoints().size() != solution.getInstance().n) {
            throw new IllegalArgumentException("Construction requires an empty solution");
        }
    }

    /** Call within a partial-solution scope. Always finishes, including after cancellation. */
    public static void fillQuotas(BMSSCSolution solution, boolean shuffle) {
        int[] points = CollectionUtil.toIntArray(solution.getNotAssignedPoints());
        if (shuffle) ArrayUtil.shuffle(points);
        int next = 0;
        for (int c = 0; c < solution.getInstance().k; c++) {
            int missing = solution.getInstance().getClusterSize(c) - solution.getClusterSize(c);
            for (int i = 0; i < missing; i++) {
                new AssignMove(solution, points[next++], c).execute(solution);
            }
        }
        if (next != points.length || !solution.feasibleClusterSizes()) {
            throw new IllegalArgumentException("Cannot complete cluster quotas");
        }
    }

    /** Seed an empty solution. An interrupted prefix can be completed by any reconstructive. */
    public static void seed(BMSSCSolution solution, SeedStrategy strategy) {
        requireEmpty(solution);
        var instance = solution.getInstance();
        int[] order = new int[instance.n];
        for (int p = 0; p < instance.n; p++) order[p] = p;
        if (strategy == SeedStrategy.RANDOM) ArrayUtil.shuffle(order);
        double[] nearest = new double[instance.n];
        Arrays.fill(nearest, Double.POSITIVE_INFINITY);
        int chosen = strategy == SeedStrategy.RANDOM ? order[0] : RandomManager.getRandom().nextInt(instance.n);
        for (int c = 0; c < instance.k && !TimeControl.isTimeUp(); c++) {
            if (strategy == SeedStrategy.RANDOM) chosen = order[c];
            new AssignMove(solution, chosen, c).execute(solution);
            if (strategy == SeedStrategy.RANDOM || c + 1 == instance.k) continue;
            double farthest = Double.NEGATIVE_INFINITY;
            int next = -1;
            for (int p = 0; p < instance.n; p++) {
                if (TimeControl.isTimeUp()) return;
                if (solution.isAssigned(p)) continue;
                nearest[p] = Math.min(nearest[p], instance.distance(p, chosen));
                if (nearest[p] > farthest) {
                    farthest = nearest[p];
                    next = p;
                }
            }
            chosen = next;
        }
    }

    public static int[] eligibleClusters(BMSSCSolution solution, int minimumSize) {
        int[] clusters = new int[solution.getInstance().k];
        int size = 0;
        for (int c = 0; c < clusters.length; c++) {
            if (solution.getClusterSize(c) >= minimumSize) clusters[size++] = c;
        }
        return Arrays.copyOf(clusters, size);
    }

    /** Partially shuffle a private array and return its first count entries. */
    public static int[] sample(int[] values, int count) {
        var random = RandomManager.getRandom();
        for (int i = 0; i < count; i++) {
            int chosen = random.nextInt(i, values.length);
            int old = values[i];
            values[i] = values[chosen];
            values[chosen] = old;
        }
        return Arrays.copyOf(values, count);
    }

    /** The supplier returns null when exhausted; moves are generated only as the search consumes them. */
    public static <M extends BMSSCMove> ExploreResult<M, BMSSCSolution, BMSSCInstance> moves(Supplier<M> next) {
        var spliterator = new Spliterators.AbstractSpliterator<M>(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL) {
            @Override
            public boolean tryAdvance(Consumer<? super M> action) {
                if (TimeControl.isTimeUp()) return false;
                M move = next.get();
                if (move == null) return false;
                action.accept(move);
                return true;
            }
        };
        return ExploreResult.fromStream(StreamSupport.stream(spliterator, false));
    }

    /** Delta for replacing a small group in one cluster, including all interactions within the groups. */
    public static double replacementDelta(BMSSCSolution solution, int cluster, int[] removed, int[] added) {
        double delta = 0;
        var instance = solution.getInstance();
        for (int i = 0; i < removed.length; i++) {
            delta -= solution.getPointClusterDistance(removed[i], cluster);
            for (int j = i + 1; j < removed.length; j++) delta += instance.distance(removed[i], removed[j]);
        }
        for (int i = 0; i < added.length; i++) {
            delta += solution.getPointClusterDistance(added[i], cluster);
            for (int point : removed) delta -= instance.distance(added[i], point);
            for (int j = i + 1; j < added.length; j++) delta += instance.distance(added[i], added[j]);
        }
        return delta / solution.getClusterSize(cluster);
    }

    public static int removalCount(BMSSCSolution solution, double fraction, int strength) {
        if (strength < 0) throw new IllegalArgumentException("Negative destruction strength");
        if (!solution.feasibleClusterSizes()) throw new IllegalArgumentException("Destruction requires a feasible solution");
        return (int) Math.min(solution.getInstance().n, Math.ceil(solution.getInstance().n * fraction * strength));
    }

    /** Return a replacement partial solution, preserving the input and suppressing its incomplete objective. */
    public static BMSSCSolution retain(BMSSCSolution original, boolean[] removed) {
        return BMSSCUtil.withPartialSolution(() -> {
            var partial = new BMSSCSolution(original.getInstance());
            for (int p = 0; p < removed.length; p++) {
                if (!removed[p]) new AssignMove(partial, p, original.clusterOf(p)).execute(partial);
            }
            return partial;
        });
    }
}

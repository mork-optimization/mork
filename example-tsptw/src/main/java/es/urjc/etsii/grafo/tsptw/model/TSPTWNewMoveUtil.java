package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.Collections;

/** Move evaluation used only by the New components. Scratch storage belongs to each invocation. */
public final class TSPTWNewMoveUtil {
    private TSPTWNewMoveUtil() {}

    public static void requireFeasible(TSPTWSolution solution) {
        if (solution.constraint_violations() != 0) {
            throw new IllegalArgumentException("Cost search and feasible shaking require a feasible tour");
        }
    }

    static int[] route(TSPTWSolution solution) {
        int[] result = new int[solution.n + 1];
        for (int i = 0; i < result.length; i++) result[i] = solution.permutation.get(i);
        return result;
    }

    /** Destination is the start position of the block in the resulting tour. */
    static void relocate(int[] source, int[] target, int from, int to, int length) {
        System.arraycopy(source, 0, target, 0, source.length);
        if (from < to) {
            System.arraycopy(source, from + length, target, from, to - from);
        } else {
            System.arraycopy(source, to, target, to + length, from - to);
        }
        System.arraycopy(source, from, target, to, length);
    }

    static double relocationDelta(TSPTWInstance instance, int[] tour, int from, int to, int length) {
        int a = tour[from - 1], b = tour[from], c = tour[from + length - 1], d = tour[from + length];
        int e = tour[from < to ? to + length - 1 : to - 1];
        int f = tour[from < to ? to + length : to];
        return instance.dist(a, d) + instance.dist(e, b) + instance.dist(c, f)
                - instance.dist(a, b) - instance.dist(c, d) - instance.dist(e, f);
    }

    /** Does not change the solution. On success, arrivals describes the entire candidate tour. */
    static boolean evaluateFeasible(TSPTWSolution solution, int[] candidate, double[] arrivals, int first, int last) {
        System.arraycopy(solution._makespan, 0, arrivals, 0, first);
        double time = arrivals[first - 1];
        for (int i = first; i <= solution.n; i++) {
            if (TimeControl.isTimeUp()) return false;
            int customer = candidate[i];
            time = Math.max(time + solution.distance[candidate[i - 1]][customer], solution.window_start[customer]);
            if (time > solution.window_end[customer]) return false;
            arrivals[i] = time;
            // After the changed segment, equal arrival times imply an identical remaining schedule.
            if (i > last && time == solution._makespan[i]) {
                System.arraycopy(solution._makespan, i + 1, arrivals, i + 1, solution.n - i);
                return true;
            }
        }
        return true;
    }

    static void commit(TSPTWSolution solution, int[] candidate, double[] arrivals, double cost) {
        for (int i = 1; i < solution.n; i++) solution.permutation.set(i, candidate[i]);
        System.arraycopy(arrivals, 0, solution._makespan, 0, arrivals.length);
        solution._tourcost = cost;
        solution._constraint_violations = 0;
        solution._infeasibility = 0;
        solution._lower_bound = -1;
        solution._lower_bound_constraint_violations = -1;
        solution.notifyUpdate();
        solution.assert_solution();
    }

    /** Full block-relocation neighborhood. A block of length one is ordinary insertion. */
    public static boolean improveRelocation(TSPTWSolution solution, int length, boolean firstImprovement) {
        requireFeasible(solution);
        if (length < 1) throw new IllegalArgumentException("Block length must be positive");
        if (TimeControl.isTimeUp() || length >= solution.n - 1) return false;
        int[] original = route(solution), candidate = new int[original.length], best = new int[original.length];
        double[] arrivals = new double[original.length], bestArrivals = new double[original.length];
        double bestCost = solution.cost();
        var positions = CollectionUtil.generateIntegerList(1, solution.n - length + 1);
        CollectionUtil.shuffle(positions);
        for (int from : positions) {
            if (TimeControl.isTimeUp()) break;
            for (int to = 1; to <= solution.n - length && !TimeControl.isTimeUp(); to++) {
                if (from == to) continue;
                double cost = solution.cost() + relocationDelta(solution.getInstance(), original, from, to, length);
                if (cost >= bestCost) continue;
                relocate(original, candidate, from, to, length);
                if (!evaluateFeasible(solution, candidate, arrivals, Math.min(from, to), Math.max(from, to) + length - 1)) continue;
                if (firstImprovement) {
                    commit(solution, candidate, arrivals, cost);
                    return true;
                }
                bestCost = cost;
                System.arraycopy(candidate, 0, best, 0, candidate.length);
                System.arraycopy(arrivals, 0, bestArrivals, 0, arrivals.length);
            }
        }
        if (bestCost >= solution.cost()) return false;
        // A completed best candidate remains safe to commit when a later evaluation is interrupted.
        commit(solution, best, bestArrivals, bestCost);
        return true;
    }

    static double swapDelta(TSPTWInstance instance, int[] tour, int first, int second) {
        double delta = 0;
        for (int k = 0; k < 4; k++) {
            int edge = switch (k) { case 0 -> first - 1; case 1 -> first; case 2 -> second - 1; default -> second; };
            if (k == 2 && edge == first) continue; // Adjacent exchanges share an edge.
            int a = tour[edge], b = tour[edge + 1];
            int newA = edge == first ? tour[second] : edge == second ? tour[first] : a;
            int newB = edge + 1 == first ? tour[second] : edge + 1 == second ? tour[first] : b;
            delta += instance.dist(newA, newB) - instance.dist(a, b);
        }
        return delta;
    }

    public static boolean improveSwap(TSPTWSolution solution) {
        requireFeasible(solution);
        if (TimeControl.isTimeUp()) return false;
        int[] original = route(solution), candidate = new int[original.length];
        double[] arrivals = new double[original.length];
        var positions = CollectionUtil.generateIntegerList(1, solution.n);
        CollectionUtil.shuffle(positions);
        for (int first : positions) {
            if (TimeControl.isTimeUp()) break;
            for (int second = first + 1; second < solution.n && !TimeControl.isTimeUp(); second++) {
                double cost = solution.cost() + swapDelta(solution.getInstance(), original, first, second);
                if (cost >= solution.cost()) continue;
                System.arraycopy(original, 0, candidate, 0, original.length);
                candidate[first] = original[second];
                candidate[second] = original[first];
                if (!evaluateFeasible(solution, candidate, arrivals, first, second)) continue;
                commit(solution, candidate, arrivals, cost);
                return true;
            }
        }
        return false;
    }

    public static void shakeRelocations(TSPTWSolution solution, int level, int attemptFactor) {
        TSPTWUtil.requireMinimumSize(solution.getInstance());
        requireFeasible(solution);
        if (level < 1 || attemptFactor < 1) throw new IllegalArgumentException("Shake strength and attempt factor must be positive");
        if (TimeControl.isTimeUp()) return;
        int target = Math.min(level, solution.n - 1), accepted = 0;
        int[] current = route(solution), candidate = new int[current.length];
        double[] arrivals = new double[current.length];
        var random = RandomManager.getRandom();
        for (long attempt = 0; attempt < (long) attemptFactor * target && accepted < target && !TimeControl.isTimeUp(); attempt++) {
            int from = 1 + random.nextInt(solution.n - 1);
            int to = 1 + random.nextInt(solution.n - 2);
            if (to >= from) to++;
            if (TimeControl.isTimeUp()) break;
            relocate(current, candidate, from, to, 1);
            if (!evaluateFeasible(solution, candidate, arrivals, Math.min(from, to), Math.max(from, to))) continue;
            double cost = solution.cost() + relocationDelta(solution.getInstance(), current, from, to, 1);
            commit(solution, candidate, arrivals, cost);
            System.arraycopy(candidate, 0, current, 0, current.length);
            accepted++;
        }
    }

    /** Same random order, gains, and pruning outcomes as the original symmetric 2-opt search. */
    public static boolean improveTwoOpt(TSPTWSolution solution) {
        requireFeasible(solution);
        if (!solution.getInstance().isSymmetric()) throw new IllegalArgumentException("2-opt requires a symmetric distance matrix");
        if (TimeControl.isTimeUp()) return false;
        double[] arrivals = new double[solution.n + 1];
        var positions = CollectionUtil.generateIntegerList(0, solution.n);
        CollectionUtil.shuffle(positions);
        var p = solution.permutation;
        var d = solution.distance;
        while (!positions.isEmpty() && !TimeControl.isTimeUp()) {
            int first = positions.removeLast();
            int c1 = p.get(first), s1 = p.get(first + 1);
            for (int last = first + 2; last < solution.n && !TimeControl.isTimeUp(); last++) {
                int c2 = p.get(last), s2 = p.get(last + 1);
                if (solution.getInstance().isTimeWindowInfeasible(c2, s1)) break;
                double gain = d[c1][c2] + d[s1][s2] - d[c1][s1] - d[c2][s2];
                if (gain >= 0) continue;
                int infeasible = evaluateTwoOpt(solution, first, last, arrivals);
                if (infeasible == 3) return false;
                if (infeasible == 2) break;
                if (infeasible == 1) continue;
                Collections.reverse(p.subList(first + 1, last + 1));
                System.arraycopy(arrivals, first + 1, solution._makespan, first + 1, solution.n - first);
                solution._tourcost += gain;
                solution.notifyUpdate();
                solution.assert_solution();
                return true;
            }
        }
        return false;
    }

    // 0: feasible; 1: incoming arc/suffix violation; 2: reversed segment violation; 3: interrupted.
    static int evaluateTwoOpt(TSPTWSolution solution, int first, int last, double[] arrivals) {
        var p = solution.permutation;
        double time = solution._makespan[first];
        int previous = p.get(first);
        for (int position = first + 1; position <= solution.n; position++) {
            if (TimeControl.isTimeUp()) return 3;
            int customer = p.get(position <= last ? first + last + 1 - position : position);
            time = Math.max(time + solution.distance[previous][customer], solution.window_start[customer]);
            if (time > solution.window_end[customer]) return position > first + 1 && position <= last ? 2 : 1;
            arrivals[position] = time;
            if (position > last && time == solution._makespan[position]) {
                System.arraycopy(solution._makespan, position + 1, arrivals, position + 1, solution.n - position);
                return 0;
            }
            previous = customer;
        }
        return 0;
    }
}

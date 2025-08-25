package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.aop.TimeStats;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.random.RandomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class TSPTWSolution extends Solution<TSPTWSolution, TSPTWInstance> {

    private static Logger log = LoggerFactory.getLogger(TSPTWSolution.class);

    List<Integer> permutation;
    // TODO remove n and instance properties, keeping them for translation of c++
    int n;
    double[][] distance;
    int[] window_start;
    int[] window_end;
    // END instance properties

    boolean[] node_assigned;
    boolean[][] tw_infeasible;
    int nodes_available;
    static int evaluations = 0;
    int _constraint_violations;
    double _infeasibility;
    int _lower_bound;
    int _lower_bound_constraint_violations;

    double[] _makespan;
    double _tourcost;

    public TSPTWSolution(TSPTWInstance instance) {
        super(instance);

        this.n = instance.n();
        this.distance = instance.getDistance();
        this.window_end = instance.getWindowEnd();
        this.window_start = instance.getWindowStart();

        this.permutation = new ArrayList<>(instance.n() + 1);
        this.node_assigned = new boolean[instance.n()];
        this.nodes_available = n;
        this._constraint_violations = 0;
        this._infeasibility = 0;
        this._lower_bound = -1;
        this._lower_bound_constraint_violations = -1;
        this._makespan = new double[instance.n() + 1];
        this._tourcost = 0;

        // todo no veo donde se usa quien lo inicializa etc, quiza borrar
        this.tw_infeasible = new boolean[instance.n()][instance.n()];

        this.permutation.add(0);
        node_assigned[0] = true;
        nodes_available--;
    }

    public TSPTWSolution(TSPTWSolution solution) {
        super(solution);
        this.copy_from(solution);
    }

    public void copy_from(TSPTWSolution solution) {
        this.n = solution.n;
        this.distance = solution.distance;
        this.window_start = solution.window_start;
        this.window_end = solution.window_end;

        this.nodes_available = solution.nodes_available;
        this.permutation = new ArrayList<>(solution.permutation);
        this.node_assigned = solution.node_assigned.clone();
        this._constraint_violations = solution._constraint_violations;
        this._infeasibility = solution._infeasibility;
        this._lower_bound = solution._lower_bound;
        this._lower_bound_constraint_violations = solution._lower_bound_constraint_violations;
        this._makespan = solution._makespan.clone();
        this._tourcost = solution._tourcost;

        // TODO review and possible delete
        this.tw_infeasible = new boolean[solution.tw_infeasible.length][];
        for (int i = 0; i < solution.tw_infeasible.length; i++) {
            this.tw_infeasible[i] = solution.tw_infeasible[i].clone();
        }
    }


    @Override
    public TSPTWSolution cloneSolution() {
        return new TSPTWSolution(this);
    }

    public TSPTWSolution clone_solution() {
        return new TSPTWSolution(this);
    }

    public double getScore() {
        return this.cost();
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(this._tourcost, this.permutation);
    }

    public double cost() {
        return this._tourcost;
    }

    public boolean better_than(TSPTWSolution other) {
        return _constraint_violations < other._constraint_violations ||
                (_constraint_violations == other._constraint_violations && cost() < other.cost());
    }

    public int constraint_violations() {
        return this._constraint_violations;
    }

    public void add(int current, int node) {
        assert (node > 0);
        assert (node != current);
        assert (!node_assigned[node]);

        permutation.add(node);
        int j = permutation.size() - 1;

        _makespan[j] = max(_makespan[j - 1] + distance[current][node],
                window_start[node]);

        if (_makespan[j] > window_end[node]) {
            _infeasibility += _makespan[j] - window_end[node];
            _constraint_violations++;
        }

        _tourcost += distance[current][node];

        node_assigned[node] = true;
        nodes_available--;

        evaluations++;

        if (nodes_available == 1) {
            current = node;
            node = 0;
            while (node_assigned[node]) {
                node++;
            }
            add(current, node);
            // This is the last node, so connect it to the depot.
            permutation.add(0);
            _makespan[n] = _makespan[n - 1] + distance[node][0];
            _tourcost += distance[node][0];
            if (_makespan[n] > window_end[0]) {
                _constraint_violations++;
                _infeasibility += _makespan[n] - window_end[0];
            }
            evaluations++;
        }
    }

    public void add(int[] p) {
        int last = permutation.get(permutation.size() - 1);
        int k = 0;

        while (nodes_available > 0) {
            int node = p[k];
            add(last, node);
            last = node;
            k++;
        }
    }

    public void assert_solution() {
        assert check_solution() : "Solution validation failed";
    }

    public boolean check_solution() {
        double mkspan = 0;
        double cost = 0;
        int prev = 0; // starts at the depot
        int cviols = 0;
        int cviolsUnsure = 0;
        double infeas = 0;

        assert permutation.size() - 1 == n : "Invalid: (permutation.size() == %s) != (n == %s)".formatted(permutation.size() - 1, n);

        assert is_a_permutation(permutation): "Invalid: not a permutation!";

        for (int i = 1; i < n; i++) {
            int node = permutation.get(i);

            cost += distance[prev][node];
            mkspan = max(mkspan + distance[prev][node], window_start[node]);
            assert DoubleComparator.equals(mkspan, _makespan[i]): "Invalid: makespan = %s != _makespan[%s] = %s".formatted(mkspan, i, _makespan[i]);

            if (_makespan[i] > window_end[node]) {
                cviols++;
                infeas += _makespan[i] - window_end[node];
            }
            if (DoubleComparator.equals(_makespan[i], window_end[node])) {
                cviolsUnsure++;
            }

            prev = node;
        }

        // Finish at the depot
        cost += distance[prev][0];
        assert DoubleComparator.equals(cost, _tourcost): "Invalid: real cost = %s != _tourcost = %s".formatted(cost, _tourcost);

        mkspan = max(mkspan + distance[prev][0], window_start[0]);
        assert DoubleComparator.equals(mkspan, _makespan[n]): "Invalid: makespan = %s != _makespan[%s] = %s".formatted(mkspan, n, _makespan[n]);

        if (_makespan[n] > window_end[0]) {
            cviols++;
            infeas += _makespan[n] - window_end[0];
        }
        if (DoubleComparator.equals(_makespan[n], window_end[0])) {
            cviolsUnsure++;
        }

        assert Math.abs(cviols - _constraint_violations) <= cviolsUnsure: "Invalid: real cviols = %s != _constraint_violations = %s (unsure = %s)".formatted(cviols, _constraint_violations, cviolsUnsure);
        assert _constraint_violations >= 0: "Invalid: _constraint_violations = %s < 0".formatted(_constraint_violations);
        assert _infeasibility == infeas: "Invalid: real infeas = %s != _infeasibility = %s".formatted(infeas, _infeasibility);

        return true;
    }

    // Helper methods
    public boolean is_a_permutation(List<Integer> permutation) {
        Set<Integer> set = HashSet.newHashSet(permutation.size());
        for (int i = 0; i < permutation.size() - 1; i++) {
            set.add(permutation.get(i));
        }
        return set.size() == permutation.size() -1; // -1 because depot appears twice, ignore last element
    }

    public void swap(int k) {
        assert k < n - 1;
        assert k > 0;

        int a = permutation.get(k - 1);
        int b = permutation.get(k);
        int c = permutation.get(k + 1);

        do_swap(k);

        if (_makespan[k] > window_end[b]) {
            _constraint_violations--;
            _infeasibility -= (_makespan[k] - window_end[b]);
        }
        if (_makespan[k + 1] > window_end[c]) {
            _constraint_violations--;
            _infeasibility -= (_makespan[k + 1] - window_end[c]);
        }

        _makespan[k] = max(_makespan[k - 1] + distance[a][c], window_start[c]);
        _makespan[k + 1] = max(_makespan[k] + distance[c][b], window_start[b]);

        if (_makespan[k] > window_end[c]) {
            _constraint_violations++;
            _infeasibility += (_makespan[k] - window_end[c]);
        }
        if (_makespan[k + 1] > window_end[b]) {
            _constraint_violations++;
            _infeasibility += (_makespan[k + 1] - window_end[b]);
        }

        double mkspan = _makespan[k + 1];
        int i, current, prev = b;
        for (i = k + 2; i < n + 1; i++, prev = current) {
            current = permutation.get(i);
            mkspan += distance[prev][current];

            if (_makespan[i] > window_start[current]) {
                if (_makespan[i] > window_end[current]) {
                    _constraint_violations--;
                    _infeasibility -= (_makespan[i] - window_end[current]);
                }
                if (mkspan <= window_start[current]) {
                    _makespan[i] = window_start[current];
                    mkspan = window_start[current];
                    continue;
                }
            } else {
                if (mkspan <= window_start[current]) {
                    _makespan[i] = window_start[current];
                    break;
                }
            }
            if (mkspan > window_end[current]) {
                _constraint_violations++;
                _infeasibility += (mkspan - window_end[current]);
            }
            _makespan[i] = mkspan;
        }
        evaluations += n - (k + 2);
    }

    public boolean is_feasible_swap(int k, int[] first_m) {
        var p = this.permutation;

        int last_m = (first_m[0] == n + 1) ? k + 2 : n + 1;
        int j, prev, current;
        for (j = min(first_m[0], k); j < last_m; j++) {
            _makespan[j] = max(_makespan[j - 1] + distance[p.get(j - 1)][p.get(j)], window_start[p.get(j)]);
            if (_makespan[j] > window_end[p.get(j)]) {
                first_m[0] = j;
                return false;
            }
        }

        prev = p.get(last_m - 1);
        double mkspan = _makespan[last_m - 1];
        for (j = last_m; j < n + 1; j++, prev = current) {
            current = p.get(j);
            mkspan += distance[prev][current];

            if (_makespan[j] <= window_start[current]) {
                if (mkspan <= window_start[current]) {
                    _makespan[j] = window_start[current];
                    first_m[0] = n + 1;
                    return true;
                }
            } else {
                assert (_makespan[j] <= window_end[current]);
                if (mkspan <= window_start[current]) {
                    _makespan[j] = window_start[current];
                    mkspan = window_start[current];
                    continue;
                }
            }
            if (mkspan > window_end[current]) {
                _makespan[j] = mkspan;
                first_m[0] = j;
                return false;
            }
            _makespan[j] = mkspan;
        }
        first_m[0] = n + 1;
        return true;
    }

    public double delta_swap(int k) {
        int a = permutation.get(k - 1);
        int b = permutation.get(k);
        int c = permutation.get(k + 1);
        int d = permutation.get(k + 2);

        double delta = (distance[a][c] + distance[c][b] + distance[b][d])
                - (distance[a][b] + distance[b][c] + distance[c][d]);

        evaluations += 6;
        return delta;
    }


    public double do_swap(int k) {
        double gain = delta_swap(k);
        _tourcost += gain;

        int b = permutation.get(k);
        permutation.set(k, permutation.get(k + 1));
        permutation.set(k + 1, b);

        return gain;
    }

    public boolean do_feasible_swap(int k, double[] deltaCost, int[] firstM) {
        double gain = do_swap(k);
        deltaCost[0] += gain;

        if (deltaCost[0] >= 0) {
            firstM[0] = min(k, firstM[0]);
            return false;
        }
        return is_feasible_swap(k, firstM);
    }

    public void insertion_move(int k, int i, int d) {
        log.debug("# insertion: {}:{}:{}", k, i, d);
        swap(k);
        assert check_solution() : "Solution validation failed after insertion move";
    }

    public boolean infeasible_move(int initial, int end) {
        return tw_infeasible[permutation.get(end)][permutation.get(initial)];
    }

    public TSPTWSolution localsearch_insertion(boolean first_improvement) {
        TSPTWSolution best = this.clone_solution();
        TSPTWSolution sol;

        for (int i = 1; i < n - 1; i++) {
            if (infeasible_move(i, i + 1)) {
                continue;
            }
            sol = this.clone_solution();
            sol.insertion_move(i, i, i + 1);
            if (sol.better_than(best)) {
                if (first_improvement) {
                    return sol.clone_solution();
                }
                best = sol;
            }

            TSPTWSolution orb1 = sol.clone_solution();
            for (int d = i + 1; d < n - 1; d++) {
                if (sol.infeasible_move(d, d + 1)) {
                    break;
                }
                sol.insertion_move(d, i, d + 1);
                if (sol.better_than(best)) {
                    if (first_improvement) {
                        return sol.clone_solution();
                    }
                    best = sol;
                }
            }

            sol = orb1.clone_solution();
            for (int d = i - 1; d > 0; d--) {
                if (sol.infeasible_move(d, d + 1)) {
                    break;
                }
                sol.insertion_move(d, i + 1, d);
                if (sol.better_than(best)) {
                    if (first_improvement) {
                        return sol.clone_solution();
                    }
                    best = sol;
                }
            }
        }
        return best.clone_solution();
    }

    public boolean insertion_is_feasible(int from, int to) {
        var makespan = this._makespan.clone();
        int low = min(from, to);
        int high = max(from, to);
        double mkspan = _makespan[low - 1];
        int pred_ci = permutation.get(low - 1);
        int i, ci;

        if (from < to) {
            ci = permutation.get(low + 1);
            mkspan += distance[pred_ci][ci];
            if (mkspan < window_start[ci]) {
                mkspan = window_start[ci];
            } else if (mkspan > window_end[ci]) {
                return false;
            }
            makespan[low] = mkspan;

            for (i = low + 2, pred_ci = ci;
                 i <= high;
                 i++, pred_ci = ci) {
                ci = permutation.get(i);
                mkspan += distance[pred_ci][ci];
                if (mkspan < window_start[ci]) {
                    mkspan = window_start[ci];
                } else if (mkspan > window_end[ci]) {
                    return false;
                }
                makespan[i - 1] = mkspan;
            }

            pred_ci = permutation.get(high);
            ci = permutation.get(from);
            mkspan += distance[pred_ci][ci];
            if (mkspan < window_start[ci]) {
                mkspan = window_start[ci];
            } else if (mkspan > window_end[ci]) {
                return false;
            }
            makespan[high] = mkspan;
            pred_ci = permutation.get(low);

        } else {
            ci = permutation.get(from);
            mkspan += distance[pred_ci][ci];
            if (mkspan < window_start[ci]) {
                mkspan = window_start[ci];
            } else if (mkspan > window_end[ci]) {
                return false;
            }
            makespan[low] = mkspan;

            for (i = low, pred_ci = ci;
                 i < high;
                 i++, pred_ci = ci) {
                ci = permutation.get(i);
                mkspan += distance[pred_ci][ci];
                if (mkspan < window_start[ci]) {
                    mkspan = window_start[ci];
                } else if (mkspan > window_end[ci]) {
                    return false;
                }
                makespan[i + 1] = mkspan;
            }
            pred_ci = permutation.get(high - 1);
        }

        for (i = high + 1; i < n + 1; i++) {
            pred_ci = ci;
            ci = permutation.get(i);
            mkspan += distance[pred_ci][ci];
            if (mkspan < window_start[ci]) {
                mkspan = window_start[ci];
            } else if (mkspan > window_end[ci]) {
                return false;
            }
            makespan[i] = mkspan;
        }
        System.arraycopy(makespan, low, this._makespan, low, this._makespan.length - low);

        return true;
    }

    public void shuffle_1shift_feasible_nodes(List<Integer> v) {
        v.clear();

        for (int i = 1; i < n - 1; i++) {
            int ci = permutation.get(i);
            int cj = permutation.get(i + 1);
            if (tw_infeasible[cj][ci]) continue;
            v.add(i);
        }
        CollectionUtil.shuffle(v);
    }

    @TimeStats
    public boolean feasible_1shift_first() {
        assert (_constraint_violations == 0);

        List<Integer> rand_nodes = new ArrayList<>();
        shuffle_1shift_feasible_nodes(rand_nodes);

        for (int k = 0; k < rand_nodes.size(); k++) {
            int i = rand_nodes.get(k);
            int ci = permutation.get(i);
            double delta1 = distance[permutation.get(i - 1)][ci]
                    + distance[ci][permutation.get(i + 1)]
                    - distance[permutation.get(i - 1)][permutation.get(i + 1)];

            // This is in fact swapping backwards i + 1
            for (int d = i - 1; d > 0; d--) {
                int cj = permutation.get(d);
                if (tw_infeasible[ci][cj]) break;
                double delta2 = distance[permutation.get(d - 1)][ci]
                        + distance[ci][cj]
                        - distance[permutation.get(d - 1)][cj];

                if (delta2 >= delta1) continue;
                if (!insertion_is_feasible(i, d)) continue;
                reinsert(permutation, i, d);
                log.debug("improved ({}, {}): {} -> {}", i, i+1, _tourcost, _tourcost - delta1 + delta2);
                this._tourcost += delta2 - delta1;
                assert_solution();
                return true;
            }

            // Swapping forward i
            for (int d = i + 2; d < n; d++) {
                int cj = permutation.get(d);
                if (tw_infeasible[cj][ci]) break;
                double delta2 = distance[ci][permutation.get(d + 1)]
                        + distance[cj][ci]
                        - distance[cj][permutation.get(d + 1)];

                if (delta2 >= delta1) continue;
                if (!insertion_is_feasible(i, d)) continue;
                reinsert(permutation, i, d);
                log.debug("improved ({}, {}): {} -> {}", i, i+1, _tourcost, _tourcost - delta1 + delta2);
                this._tourcost += delta2 - delta1;
                assert_solution();
                return true;
            }
        }
        return false;
    }

    public boolean ls_feasibility_1shift_first() {
        boolean improved = false;
        while (feasibility_1shift_first_code()) {
            improved = true;
        }
        return improved;
    }

    public boolean feasibility_1shift_first_code() {
        if (_constraint_violations == 0) return false;

        boolean[] improved = {false};

        assert_solution();
        if (backward_violated(improved)) return true;
        assert_solution();
        if (forward_nonviolated(improved)) return true;
        assert_solution();
        if (forward_violated(improved)) return true;
        assert_solution();
        if (backward_nonviolated(improved)) return true;
        assert_solution();

        return improved[0];
    }


    public boolean feasibility_1shift_first_paper() {
        if (_constraint_violations == 0) return false;

        boolean[] improved = {false};

        if (backward_violated(improved)) return true;
        if (forward_nonviolated(improved)) return true;
        if (backward_nonviolated(improved)) return true;
        if (forward_violated(improved)) return true;

        return improved[0];
    }

    public boolean backward_violated(boolean[] improved) {
        List<Integer> infeas = new ArrayList<>();
        compute_infeas_set(infeas);

        TSPTWSolution sol;

        // Backward movements of violated customers.
        do {
            this.assert_solution();
            int i = infeas.removeLast();
            assert (_makespan[i] > window_end[i]);
            sol = this.clone_solution();
            boolean moved = false;

            for (int d = i - 1; d > 0; d--) {
                if (sol.infeasible_move(d, d + 1)) break;
                sol.swap(d);
                if (sol.infeasibility() < this.infeasibility()) {
                    this.copy_from(sol);
                    improved[0] = true;
                    moved = true;
                    if (sol.infeasibility() == 0) return true;
                }
            }
            if (moved) {
                compute_infeas_set(infeas);
            }
        } while (infeas.size() > 0);
        return false;
    }

    public boolean forward_nonviolated(boolean[] improved) {
        List<Integer> feas = new ArrayList<>();
        compute_feas_set(feas);

        TSPTWSolution sol;

        // Forward movements of non-violated customers.
        while (!feas.isEmpty()) {
            int i = feas.remove(feas.size() - 1);
            assert (_makespan[i] <= window_end[i]);
            sol = this.clone_solution();
            boolean moved = false;

            for (int d = i; d < n - 1; d++) {
                if (sol.infeasible_move(d, d + 1)) break;
                sol.swap(d);
                if (sol.infeasibility() < this.infeasibility()) {
                    this.copy_from(sol);
                    improved[0] = true;
                    moved = true;
                    if (this.infeasibility() == 0) return true;
                }
            }
            if (moved) {
                compute_feas_set(feas);
            }
        }
        return false;
    }

    public boolean forward_violated(boolean[] improved) {
        List<Integer> infeas = new ArrayList<>();
        compute_infeas_set(infeas);

        TSPTWSolution sol;

        // Forward movements of violated customers.
        while (!infeas.isEmpty()) {
            int i = infeas.remove(infeas.size() - 1);
            assert (_makespan[i] > window_end[i]);
            sol = this.clone_solution();
            boolean moved = false;

            for (int d = i; d < n - 1; d++) {
                if (sol.infeasible_move(d, d + 1)) break;
                sol.swap(d);
                if (sol.infeasibility() < this.infeasibility()) {
                    this.copy_from(sol);
                    improved[0] = true;
                    moved = true;
                    if (this.infeasibility() == 0) return true;
                }
            }
            if (moved) {
                compute_infeas_set(infeas);
            }
        }
        return false;
    }

    public boolean backward_nonviolated(boolean[] improved) {
        List<Integer> feas = new ArrayList<>();
        compute_feas_set(feas);

        TSPTWSolution sol;

        // Backward movements of non-violated customers.
        while (!feas.isEmpty()) {
            int i = feas.remove(feas.size() - 1);
            assert (_makespan[i] <= window_end[i]);
            sol = this.clone_solution();
            boolean moved = false;

            for (int d = i - 1; d > 0; d--) {
                if (sol.infeasible_move(d, d + 1)) break;
                sol.swap(d);
                if (sol.infeasibility() < this.infeasibility()) {
                    this.copy_from(sol);
                    improved[0] = true;
                    moved = true;
                    if (sol.infeasibility() == 0) return true;
                }
            }
            if (moved) {
                compute_feas_set(feas);
            }
        }
        return false;
    }

    public void compute_feas_set(List<Integer> feas) {
        feas.clear();

        for (int i = 1; i < n; i++) {
            if (_makespan[i] <= window_end[i]) {
                feas.add(i);
            }
        }
        CollectionUtil.shuffle(feas);
    }

    public void compute_infeas_set(List<Integer> infeas) {
        infeas.clear();

        for (int i = 1; i < n; i++) {
            if (_makespan[i] > window_end[i]) {
                infeas.add(i);
            }
        }
        CollectionUtil.shuffle(infeas);
    }

    public double infeasibility() {
        return this._infeasibility;
    }

    public boolean feasibility_1shift_first() {
        if (_constraint_violations == 0) return false;

        boolean improved = false;
        TSPTWSolution sol = this.clone_solution();

        // Backward movements of violated customers.
        for (int i = 2; i < n; i++) {
            if (_makespan[i] <= window_end[i]) continue;
            sol = this.clone_solution();
            for (int d = i - 1; d > 0; d--) {
                if (sol.infeasible_move(d, d + 1)) break;
                sol.swap(d);
                if (sol.infeasibility() < this.infeasibility()) {
                    this.copy_from(sol);
                    improved = true;
                    if (sol.infeasibility() == 0) return true;
                }
            }
        }

        for (int i = 1; i < n - 1; i++) {
            if (_makespan[i] > window_end[i]) continue;
            if (infeasible_move(i, i + 1)) continue;
            sol = this.clone_solution();

            // Forward movements of non-violated customers.
            sol.swap(i);
            if (sol.infeasibility() < this.infeasibility()) {
                this.copy_from(sol);
                improved = true;
                if (sol.infeasibility() == 0) return true;
            }
            TSPTWSolution back_sol = sol.clone_solution();

            for (int d = i + 1; d < n - 1; d++) {
                if (sol.infeasible_move(d, d + 1)) break;
                sol.swap(d);
                if (sol.infeasibility() < this.infeasibility()) {
                    this.copy_from(sol);
                    improved = true;
                    if (sol.infeasibility() == 0) return true;
                }
            }

            // Backward movements of non-violated customers.
            for (int d = i - 1; d > 0; d--) {
                if (back_sol.infeasible_move(d, d + 1)) break;
                back_sol.swap(d);
                if (back_sol.infeasibility() < this.infeasibility()) {
                    this.copy_from(back_sol);
                    improved = true;
                    if (this.infeasibility() == 0) return true;
                }
            }
        }

        // Forward movements of violated customers.
        for (int i = 1; i < n - 1; i++) {
            if (_makespan[i] <= window_end[i]) continue;
            sol = this.clone_solution();
            for (int d = i; d < n - 1; d++) {
                if (sol.infeasible_move(d, d + 1)) break;
                sol.swap(d);
                if (sol.infeasibility() < this.infeasibility()) {
                    this.copy_from(sol);
                    improved = true;
                    if (this.infeasibility() == 0) return true;
                }
            }
        }

        return improved;
    }

    public int two_opt_is_infeasible(int h1, int h3) {
        double[] makespan = this._makespan.clone();
        double mkspan = this._makespan[h1];
        int pred_ci = permutation.get(h1);
        int ci = permutation.get(h3);
        mkspan += distance[pred_ci][ci];
        if (mkspan < window_start[ci]) {
            mkspan = window_start[ci];
        } else if (mkspan > window_end[ci]) {
            return 1;
        }
        makespan[h1 + 1] = mkspan;
        int i = h3 - 1;
        int j = h1 + 2;
        pred_ci = permutation.get(h3);
        while (i >= h1 + 1) {
            ci = permutation.get(i);
            mkspan += distance[pred_ci][ci];
            if (mkspan < window_start[ci]) {
                mkspan = window_start[ci];
            } else if (mkspan > window_end[ci]) {
                return 2;
            }
            pred_ci = ci;
            makespan[j] = mkspan;
            i--;
            j++;
        }
        for (i = h3 + 1, pred_ci = permutation.get(h1 + 1); i < n + 1; i++, pred_ci = ci) {
            ci = permutation.get(i);
            mkspan += distance[pred_ci][ci];
            if (makespan[i] <= window_start[ci]) {
                if (mkspan <= window_start[ci]) {
                    makespan[i] = window_start[ci];
                    i++;
                    break;
                }
            } else {
                if (mkspan <= window_start[ci]) {
                    mkspan = makespan[i] = window_start[ci];
                    continue;
                }
            }
            if (mkspan > window_end[ci]) {
                return 1;
            }
            makespan[i] = mkspan;
        }
        System.arraycopy(makespan, h1 + 1, this._makespan, h1 + 1, i - (h1 + 1));
        return 0;
    }

    public void two_opt_move(int h1, int h3) {
        Collections.reverse(permutation.subList(h1 + 1, h3 + 1));
    }

    @TimeStats
    public boolean two_opt_first() {
        assert this.getInstance().isSymmetric();
        assert _constraint_violations == 0;

        boolean improved = false;
        List<Integer> rand_nodes = generate_vector(n);

        while (!rand_nodes.isEmpty()) {
            int pos_c1 = rand_nodes.remove(rand_nodes.size() - 1);
            int c1 = permutation.get(pos_c1);
            int s1 = permutation.get(pos_c1 + 1);
            double radius = distance[c1][s1];

            for (int h = pos_c1 + 2; h < n; h++) {
                int pos_c2 = h;
                int c2 = permutation.get(pos_c2);
                if (tw_infeasible[c2][s1]) break;
                int s2 = permutation.get(h + 1);
                double gain = distance[c1][c2] + distance[s1][s2] - radius - distance[c2][s2];
                if (gain >= 0) continue;

                int infeas = two_opt_is_infeasible(pos_c1, pos_c2);
                if (infeas == 2) {
                    break;
                } else if (infeas == 1) {
                    continue;
                } else {
                    assert infeas == 0;
                    two_opt_move(pos_c1, pos_c2);
                    _tourcost += gain;
                    assert_solution();
                    improved = true;
                    rand_nodes = generate_vector(n);
                    break;
                }
            }
        }
        return improved;
    }

    public TSPTWSolution localsearch_2opt_first() {
        TSPTWSolution s = this.clone_solution();
        s.two_opt_first();
        return s;
    }

    public void full_eval() {
        TSPTWSolution tmp = new TSPTWSolution(this.getInstance());
        List<Integer> sub = new ArrayList<>(this.permutation.subList(1, this.permutation.size() - 1));
        tmp.add(sub.stream().mapToInt(Integer::intValue).toArray());
        this.copy_from(tmp);
    }

    @TimeStats
    public void perturb_1shift_feasible(int level) {
        var rng = RandomManager.getRandom();
        assert _constraint_violations == 0;
        assert level > 0;
        int num = min(n, level);

        List<Integer> index = new ArrayList<>();
        for (int k = 0; k < n - 2; k++) {
            index.add(k + 1);
        }

        TSPTWSolution ngh;

        for (int j = n - 3; j >= 0; j--) {
            int k = rng.nextInt(j + 1);
            Collections.swap(index, k, j);
            k = index.get(j);
            assert k > 0 && k < n;
            int pos = 1 + rng.nextInt(n - 3);
            if (pos == k) continue;

            ngh = this.clone_solution();
            double delta_cost = 0;
            int first_m = n + 1;

            if (k < pos) { // Forward
                for (int d = k; d < pos; d++) {
                    int ci = ngh.permutation.get(d);
                    int cj = ngh.permutation.get(d + 1);
                    if (tw_infeasible[cj][ci]) break;
                    delta_cost += ngh.do_swap(d);
                    var first_m_t = new int[]{first_m};
                    boolean is_feasible = ngh.is_feasible_swap(d, first_m_t);
                    first_m = first_m_t[0];
                    if (is_feasible) {
                        assert ngh.constraint_violations() == 0;
                        assert this._tourcost + delta_cost == ngh._tourcost;
                        assert first_m == n + 1;
                        this.copy_from(ngh);
                        assert_solution();
                        delta_cost = 0;
                    }
                }
            } else { // Backward
                for (int d = k - 1; d >= pos; d--) {
                    int ci = ngh.permutation.get(d);
                    int cj = ngh.permutation.get(d + 1);
                    if (tw_infeasible[cj][ci]) break;
                    delta_cost += ngh.do_swap(d);
                    var first_m_t = new int[]{first_m};
                    boolean is_feasible = ngh.is_feasible_swap(d, first_m_t);
                    first_m = first_m_t[0];
                    if (is_feasible) {
                        assert ngh.constraint_violations() == 0;
                        assert this._tourcost + delta_cost == ngh._tourcost;
                        assert first_m == n + 1;
                        this.copy_from(ngh);
                        assert_solution();
                        delta_cost = 0;
                    }
                }
            }

            num--;
            if (num == 0) break;
        }
        assert_solution();
    }

    public void perturb_1shift(int level) {
        var rng = RandomManager.getRandom();
        assert level > 0;
        int num = min(n, level);
        int earliest = n;

        do {
            int k = 1 + rng.nextInt(n - 1);
            int pos;
            do {
                pos = 1 + rng.nextInt(n - 1);
            } while (pos == k);

            earliest = min(earliest, min(pos, k));
            int tmp = permutation.get(k);
            reinsert(permutation, tmp, k, pos);
            num--;
        } while (num > 0);

        if (earliest < n) {
            full_eval();
            assert_solution();
        }
    }

    public static <T> void reinsert(List<T> v, T element, int from, int to) {
        v.remove(from);
        v.add(to, element);
    }

    public static <T> void reinsert(List<T> v, int from, int to) {
        T element = v.get(from);
        v.remove(from);
        v.add(to, element);
    }

    public static List<Integer> generate_vector(int size) {
        var random = RandomManager.getRandom();
        List<Integer> v = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            v.add(i);
        }

        for (int i = 0; i < size - 1; i++) {
            int j = (int) (random.nextDouble() * (size - i));
            int temp = v.get(i);
            v.set(i, v.get(i + j));
            v.set(i + j, temp);
        }

        return v;
    }
}

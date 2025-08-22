package es.urjc.etsii.grafo.TSPTW.model;

import es.urjc.etsii.grafo.solution.Solution;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

public class TSPTWSolution extends Solution<TSPTWSolution, TSPTWInstance> {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(TSPTWSolution.class);

    List<Integer> permutation;
    // TODO remove n and instance properties, keeping them for translation of c++
    int n;
    int[][] distance;
    int[] window_start;
    int[] window_end;
    // END instance properties

    boolean[] node_assigned;
    int nodes_available;
    static int evaluations = 0;
    int _constraint_violations;
    int _infeasibility;
    int _lower_bound;
    int _lower_bound_constraint_violations;

    int[] _makespan;
    int _tourcost;

    public TSPTWSolution(TSPTWInstance instance) {
        super(instance);

        this.n = instance.n();
        this.distance = instance.getDistance();
        this.window_end = instance.getWindowEnd();
        this.window_start = instance.getWindowStart();

        this.permutation = new ArrayList<>(instance.n() + 1);
        this.node_assigned = new boolean[instance.n()];
        this._constraint_violations = 0;
        this._infeasibility = 0;
        this._lower_bound = -1;
        this._lower_bound_constraint_violations = -1;
        this._makespan = new int[instance.n() + 1];
        this._tourcost = 0;

        this.permutation.add(0);
        node_assigned[0] = true;
        nodes_available--;
    }

    public TSPTWSolution(TSPTWSolution solution) {
        super(solution);

        this.n = solution.n;
        this.distance = solution.distance;
        this.window_start = solution.window_start;
        this.window_end = solution.window_end;

        this.permutation = new ArrayList<>(solution.permutation);
        this.node_assigned = solution.node_assigned.clone();
        this._constraint_violations = solution._constraint_violations;
        this._infeasibility = solution._infeasibility;
        this._lower_bound = solution._lower_bound;
        this._lower_bound_constraint_violations = solution._lower_bound_constraint_violations;
        this._makespan = solution._makespan.clone();
        this._tourcost = solution._tourcost;
    }


    @Override
    public TSPTWSolution cloneSolution() {
        return new TSPTWSolution(this);
    }

    public double getScore() {
        return this.cost();
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(this._tourcost, this.permutation);
    }

//    public void setTour(int[] tour) {
//        this.tour = tour.clone();
//        this.score = recalculateScore();
//        notifyUpdate();
//    }

    public double cost() {
        return this._tourcost;
    }

    public boolean better_than(TSPTWSolution other) {
        return _constraint_violations < other._constraint_violations ||
                (_constraint_violations == other._constraint_violations && cost() < other.cost());
    }

    int constraint_violations() {
        return this._constraint_violations;
    }

    void add(int current, int node) {
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


}

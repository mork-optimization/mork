package es.urjc.etsii.grafo.mdp.constructives;

import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.mdp.model.MDPInstance;
import es.urjc.etsii.grafo.mdp.model.MDPNode;
import es.urjc.etsii.grafo.mdp.model.MDPSolution;
import es.urjc.etsii.grafo.mdp.util.Weighted;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * GRASP "D2" constructive (the "good value" constructive of the Scatter Search). Starting from the
 * full solution Mork provides, it repeatedly removes the node that contributes <b>least</b> to the
 * total diversity until {@code m} nodes remain. Instead of always removing the single worst node it
 * picks at random from a restricted candidate list (RCL) of the least-contributing nodes
 * (contribution {@code <= min + alpha·(max − min)}), which injects the randomization Scatter Search
 * needs to build a varied initial reference set.
 */
public class MDPGreedyConstructive extends Constructive<MDPSolution, MDPInstance> {

    private final double alpha;

    public MDPGreedyConstructive(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public MDPSolution construct(MDPSolution solution) {
        int target = solution.getInstance().getNumSolutionNodes();
        RandomGenerator random = RandomManager.getRandom();

        while (solution.getNumNodes() > target) {
            List<Weighted<MDPNode>> contributions = solution.getNodesDistance();
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            for (Weighted<MDPNode> w : contributions) {
                double c = w.getWeight();
                if (c < min) min = c;
                if (c > max) max = c;
            }

            // Removal candidates = least-contributing nodes (worst for diversity)
            double threshold = min + alpha * (max - min);
            List<MDPNode> rcl = new ArrayList<>();
            for (Weighted<MDPNode> w : contributions) {
                if (w.getWeight() <= threshold) {
                    rcl.add(w.getElement());
                }
            }

            solution.removeNode(rcl.get(random.nextInt(rcl.size())));
        }

        solution.notifyUpdate();
        return solution;
    }
}

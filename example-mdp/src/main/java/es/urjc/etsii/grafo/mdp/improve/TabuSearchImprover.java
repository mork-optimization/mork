package es.urjc.etsii.grafo.mdp.improve;

import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.mdp.model.MDPInstance;
import es.urjc.etsii.grafo.mdp.model.MDPNode;
import es.urjc.etsii.grafo.mdp.model.MDPSolution;
import es.urjc.etsii.grafo.mdp.util.RotateListView;
import es.urjc.etsii.grafo.mdp.util.Weighted;
import es.urjc.etsii.grafo.solution.Objective;

import java.util.List;

/**
 * Short-term memory (tabu) local search of the WITH_MEMORY Scatter Search variant, implemented as a
 * custom Mork {@link Improver} (Mork has no generic tabu-search component). Ported from the
 * {@code mork-experiments} {@code LocalSearchTabuSearch}.
 *
 * <p>Each iteration removes the worst (least-contributing) non-tabu selected node and inserts the
 * first improving non-tabu unselected node — or, if none improves, the best non-improving one — so
 * the search can climb out of local optima. Swapped nodes become tabu for a tenure proportional to
 * the problem size (a longer tenure for nodes just added to the solution, a shorter one for nodes
 * just removed). The best solution seen is tracked and restored at the end. It stops after
 * {@code maxIter} iterations without improving the incumbent.
 *
 * <p>State is held in fields (as in the port) but fully reset at the start of every {@link #improve}
 * call, so the single shared instance Mork reuses across solutions behaves correctly under
 * sequential execution.
 */
public class TabuSearchImprover extends Improver<MDPSolution, MDPInstance> {

    private static final double MAX_ITER_PERCENT = 0.1;
    private static final double TABU_TENURE_SOL_PERCENT = 0.28;
    private static final double TABU_TENURE_SET_PERCENT = 0.028;

    private int maxIter;
    private int tabuTenureSolution;
    private int tabuTenureSet;

    private int iterationCounter;
    private int[] tabuEnd;

    private MDPSolution solution;
    private int notImproveIterationCounter;

    private MDPSolution bestSolution;
    private double bestSolutionWeight;

    public TabuSearchImprover(Objective<?, MDPSolution, MDPInstance> objective) {
        super(objective);
    }

    @Override
    public MDPSolution improve(MDPSolution solution) {

        int solutionNodes = solution.getNumNodes();
        int setNodes = solution.getInstance().getNumNodes() - solutionNodes;

        maxIter = (int) (setNodes * MAX_ITER_PERCENT);
        tabuTenureSet = (int) (setNodes * TABU_TENURE_SET_PERCENT);
        tabuTenureSolution = (int) (solutionNodes * TABU_TENURE_SOL_PERCENT);

        this.solution = solution;
        solution.calculateNodesDistance();

        MDPInstance instance = solution.getInstance();

        bestSolution = new MDPSolution(solution);
        bestSolutionWeight = bestSolution.getWeight();

        iterationCounter = 0;
        notImproveIterationCounter = 0;

        List<MDPNode> nodes = instance.getNodes();
        tabuEnd = new int[nodes.size()];

        Iterable<? extends MDPNode> iterableForNodes = new RotateListView<>(nodes);

        do {

            iterationCounter++;

            Weighted<MDPNode> oldWNode = getWorstNode(solution);
            if (oldWNode == null) {
                break; // every selected node is tabu; nothing legal to remove
            }

            MDPNode oldNode = oldWNode.getElement();
            double weightWithoutOld = solution.getWeight() - oldWNode.getWeight();

            double bestChangeWeight = 0;
            MDPNode bestChangeNode = null;
            boolean nodeChanged = false;

            for (MDPNode newNode : iterableForNodes) {

                if (!solution.contains(newNode)) {

                    if (tabuEnd[newNode.getIndex()] < iterationCounter) {

                        double newWeight = weightWithoutOld
                                + solution.calculateDistanceWithoutNode(oldNode, newNode);

                        if (newWeight > solution.getWeight()) {

                            changeNode(oldNode, newNode);
                            nodeChanged = true;
                            break;

                        } else if (bestChangeWeight < newWeight) {
                            bestChangeWeight = newWeight;
                            bestChangeNode = newNode;
                        }
                    }
                }
            }

            if (!nodeChanged) {
                if (bestChangeNode == null) {
                    break; // no legal non-tabu swap available
                }
                changeNode(oldNode, bestChangeNode);
            }

        } while (notImproveIterationCounter < maxIter);

        solution.asMDPSolution(bestSolution);
        solution.calculatePreciseWeight();
        return solution;
    }

    private Weighted<MDPNode> getWorstNode(MDPSolution solution) {
        Weighted<MDPNode> oldWNode = null;
        for (Weighted<MDPNode> wn : solution.getNodesDistance()) {
            if (tabuEnd[wn.getElement().getIndex()] < iterationCounter) {
                oldWNode = Weighted.min(oldWNode, wn);
            }
        }
        return oldWNode;
    }

    private void changeNode(MDPNode oldNode, MDPNode newNode) {

        tabuEnd[oldNode.getIndex()] = iterationCounter + tabuTenureSet;
        tabuEnd[newNode.getIndex()] = iterationCounter + tabuTenureSolution;

        solution.changeNode(oldNode, newNode);

        if (solution.getWeight() > bestSolutionWeight) {
            bestSolutionWeight = solution.getWeight();
            bestSolution = new MDPSolution(solution);
            notImproveIterationCounter = 0;
        } else {
            notImproveIterationCounter++;
        }
    }
}

package es.urjc.etsii.grafo.mdp.scattersearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import es.urjc.etsii.grafo.mdp.model.MDPInstance;
import es.urjc.etsii.grafo.mdp.model.MDPNode;
import es.urjc.etsii.grafo.mdp.model.MDPSolution;
import es.urjc.etsii.grafo.mdp.util.Weighted;

/**
 * Long-term Tabu/D2 memory engine of the WITH_MEMORY Scatter Search variant. It is the piece Mork
 * has no generic component for, so it is implemented from scratch here (ported from the
 * {@code mork-experiments} hand-written Scatter Search).
 *
 * <p>It keeps, per node of the instance, how often the node has appeared in accepted solutions
 * ({@code freq}) and the running average quality of those solutions ({@code quality}). From these it
 * derives a per-node <i>ponderation</i> that rewards historically high-quality nodes
 * ({@code +DELTA·quality/maxQuality}) and penalizes over-used ones ({@code −BETA·freq/maxFreq}). A
 * solution is then built greedily by starting from a candidate node set and dropping, one by one,
 * the node with the worst <i>contribution + range·ponderation</i> until {@code m} remain — so the
 * memory pushes construction toward under-explored, high-quality nodes (long-term diversification).
 *
 * <p>The same calculator (hence the same memory) is shared by the memory-guided constructive and
 * combinator of one Scatter Search run over one instance; see {@link TabuD2Memory}.
 */
public class TabuD2Calculator {

    private static final double BETA = 0.1;
    private static final double DELTA = 0.0001;

    private final MDPInstance instance;

    private int[] freq;
    private double[] quality;

    private int maxFreq;
    private double maxQuality;

    private int numTotalNodes;

    protected double[] ponderation;

    public TabuD2Calculator(MDPInstance instance) {
        this.instance = instance;
        initFreqQuality();
    }

    private void initFreqQuality() {
        numTotalNodes = instance.getNodes().size();
        freq = new int[numTotalNodes];
        quality = new double[numTotalNodes];
        maxFreq = 0;
        maxQuality = 0;
        ponderation = new double[numTotalNodes];
    }

    public void refreshMemory(MDPSolution solution) {
        for (MDPNode node : solution) {
            int index = node.getIndex();
            quality[index] = ((quality[index] * freq[index] + solution.getWeight()))
                    / (freq[index] + 1);
            freq[index]++;
            maxFreq = Math.max(maxFreq, freq[index]);
            maxQuality = Math.max(maxQuality, quality[index]);
        }
    }

    protected void calculatePonderation(Collection<MDPNode> nodes) {
        if (maxFreq > 0) {
            for (MDPNode node : nodes) {
                int index = node.getIndex();
                ponderation[index] = (DELTA * quality[index] / maxQuality)
                        - (BETA * freq[index] / maxFreq);
            }
        }
    }

    public MDPSolution createSolutionRefreshMemory() {
        MDPSolution solution = createSolutionNoRefreshMemory();
        refreshMemory(solution);
        return solution;
    }

    public MDPSolution createSolutionRefreshMemory(Collection<MDPNode> nodes) {
        MDPSolution solution = createSolutionNoRefreshMemory(nodes);
        refreshMemory(solution);
        return solution;
    }

    public MDPSolution createSolutionNoRefreshMemory(Collection<MDPNode> nodes) {

        calculatePonderation(nodes);

        List<Weighted<MDPNode>> nodesContribution = new LinkedList<>();
        double minContribution = Double.MAX_VALUE;
        double maxContribution = 0;

        for (MDPNode n : nodes) {
            double weight = 0;
            for (MDPNode on : nodes) {
                if (n != on) {
                    weight += n.getDistanceTo(on);
                }
            }
            nodesContribution.add(new Weighted<>(n, weight));

            minContribution = Math.min(minContribution, weight);
            maxContribution = Math.max(maxContribution, weight);
        }

        tabuD2NodesContribution(nodesContribution, minContribution, maxContribution);

        return new MDPSolution(instance, nodesContribution);
    }

    public MDPSolution createSolutionNoRefreshMemory() {

        calculatePonderation(instance.getNodes());

        List<Weighted<MDPNode>> nodesContribution = new LinkedList<>();
        double minContribution = Double.MAX_VALUE;
        double maxContribution = 0;

        for (Weighted<MDPNode> wNode : instance.getNodesDistance()) {
            nodesContribution.add(new Weighted<>(wNode));
            minContribution = Math.min(minContribution, wNode.getWeight());
            maxContribution = Math.max(maxContribution, wNode.getWeight());
        }

        tabuD2NodesContribution(nodesContribution, minContribution, maxContribution);

        return new MDPSolution(instance, nodesContribution);
    }

    private void tabuD2NodesContribution(List<Weighted<MDPNode>> nodesContribution,
                                         double minContribution, double maxContribution) {

        while (nodesContribution.size() > instance.getNumSolutionNodes()) {

            MDPNode worstNode = searchWorstNode(nodesContribution, minContribution, maxContribution);

            minContribution = Double.MAX_VALUE;
            maxContribution = 0;

            for (Iterator<Weighted<MDPNode>> it = nodesContribution.iterator(); it.hasNext(); ) {

                Weighted<MDPNode> wn = it.next();

                if (wn.getElement() == worstNode) {
                    it.remove();
                } else {
                    double weight = wn.getWeight() - wn.getElement().getDistanceTo(worstNode);
                    wn.setWeight(weight);

                    minContribution = Math.min(minContribution, weight);
                    maxContribution = Math.max(maxContribution, weight);
                }
            }
        }
    }

    private MDPNode searchWorstNode(List<Weighted<MDPNode>> nodesContribution,
                                    double minContribution, double maxContribution) {
        double range = maxContribution - minContribution;

        MDPNode worstPonderatedNode = null;
        double worstPonderatedWeight = Double.MAX_VALUE;

        for (Weighted<MDPNode> wn : nodesContribution) {
            double originalDistance = wn.getWeight();
            double ponderatedWeight = originalDistance + range
                    * ponderation[wn.getElement().getIndex()];

            if (ponderatedWeight < worstPonderatedWeight) {
                worstPonderatedWeight = ponderatedWeight;
                worstPonderatedNode = wn.getElement();
            }
        }

        return worstPonderatedNode;
    }

    public MDPInstance getInstance() {
        return instance;
    }
}

package es.urjc.etsii.grafo.graphs.mvc;

import es.urjc.etsii.grafo.algorithms.cmsa.CMSAConstructive;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.graphs.model.Edge;
import es.urjc.etsii.grafo.graphs.model.MSTInstance;
import es.urjc.etsii.grafo.graphs.model.MSTSolution;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * Probabilistic constructive method for the Minimum Vertex Cover (MVC) problem, used as the
 * "Construct" step of the CMSA demo in this project.
 * <p>
 * Repeatedly picks a random still-uncovered edge, and adds one of its endpoints to the cover,
 * biased towards the endpoint with the highest remaining degree (as in the classic greedy vertex
 * cover heuristic), but not always: this randomization is what allows CMSA to sample different,
 * varied vertex subsets across iterations to build the sub-instance.
 */
public class MVCConstructive extends CMSAConstructive<MSTSolution, MSTInstance, Integer> {

    /**
     * Probability of picking the endpoint with the highest remaining degree, instead of the other one.
     */
    private final double greedyBias;

    public MVCConstructive() {
        this(0.8);
    }

    @AutoconfigConstructor
    public MVCConstructive(@RealParam(min = 0, max = 1) double greedyBias) {
        if (greedyBias < 0 || greedyBias > 1) {
            throw new IllegalArgumentException("greedyBias must be in [0, 1]");
        }
        this.greedyBias = greedyBias;
    }

    @Override
    public MSTSolution construct(MSTSolution solution) {
        var instance = solution.getInstance();
        var rnd = RandomManager.getRandom();
        var edges = new ArrayList<>(instance.getEdges());
        CollectionUtil.shuffle(edges);

        for (Edge edge : edges) {
            if (solution.isInCover(edge.from()) || solution.isInCover(edge.to())) {
                continue; // Already covered by a vertex chosen earlier
            }
            solution.addToCover(chooseEndpoint(instance, edge, rnd));
        }
        solution.setScoreCover();
        solution.notifyUpdate();
        return solution;
    }

    private int chooseEndpoint(MSTInstance instance, Edge edge, RandomGenerator rnd) {
        int degFrom = instance.getEdges(edge.from()).size();
        int degTo = instance.getEdges(edge.to()).size();
        int higherDegreeEndpoint = degFrom >= degTo ? edge.from() : edge.to();
        int lowerDegreeEndpoint = degFrom >= degTo ? edge.to() : edge.from();
        return rnd.nextDouble() < greedyBias ? higherDegreeEndpoint : lowerDegreeEndpoint;
    }

    @Override
    public Set<Integer> usedComponents(MSTSolution solution) {
        return solution.getCoverVertices();
    }

    @Override
    public String toString() {
        return "MVCConstructive{greedyBias=" + greedyBias + "}";
    }
}

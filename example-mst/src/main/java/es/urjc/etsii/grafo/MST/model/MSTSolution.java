package es.urjc.etsii.grafo.MST.model;

import es.urjc.etsii.grafo.solution.Solution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MSTSolution extends Solution<MSTSolution, MSTInstance> {

    private List<Edge> mstEdges;

    /**
     * Initialize solution from instance
     *
     * @param instance
     */
    public MSTSolution(MSTInstance instance) {
        super(instance);
        // TODO Initialize solution data structures if necessary
    }

    /**
     * Clone constructor
     *
     * @param solution Solution to clone
     */
    public MSTSolution(MSTSolution solution) {
        super(solution);
        this.mstEdges = new ArrayList<>(solution.mstEdges);
    }


    @Override
    public MSTSolution cloneSolution() {
        return new MSTSolution(this);
    }

    /**
     * Get the current solution score.
     * The difference between this method and recalculateScore is that
     * this result can be a property of the solution, or cached,
     * it does not have to be calculated each time this method is called
     *
     * @return current solution score as double
     */
    public double getScore() {
        double score = 0;
        for(var edge: mstEdges){
            score += edge.weight();
        }
        return score;
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(mstEdges);
    }

    /**
     * Set the edges of the MST solution
     *
     * @param mstEdges List of edges that form the MST
     */
    public void setEdges(List<Edge> mstEdges) {
        this.mstEdges = mstEdges;
    }

    /**
     * Generate a string representation of this solution. Used when printing progress to console,
     * show as minimal info as possible
     *
     * @return Small string representing the current solution (Example: id + score)
     */
    @Override
    public String toString() {
        return String.valueOf(this.getScore());
    }
}

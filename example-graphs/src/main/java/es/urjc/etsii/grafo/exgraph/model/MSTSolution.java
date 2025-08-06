package es.urjc.etsii.grafo.exgraph.model;

import es.urjc.etsii.grafo.solution.Solution;

import java.util.List;

public class MSTSolution extends Solution<MSTSolution, MSTInstance> {

    private double score;
    /**
     * Initialize solution from instance
     *
     * @param instance
     */
    public MSTSolution(MSTInstance instance) {
        super(instance);
        score = 0;
    }

    /**
     * Clone constructor
     *
     * @param solution Solution to clone
     */
    public MSTSolution(MSTSolution solution) {
        super(solution);
        this.score = solution.score;
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
        return score;
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

    public void setScore(double score) {
        this.score = score;
    }

    public void setScoreEdges(List<Edge> mstEdges) {
        score = 0;
        for(Edge e : mstEdges) {
            score += e.weight();
        }
    }

    public void setScoreDist(double[][] d){
        score = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d.length; j++) {
                score += d[i][j];
            }
        }
    }
}

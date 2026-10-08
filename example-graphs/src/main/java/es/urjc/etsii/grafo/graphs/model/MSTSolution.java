package es.urjc.etsii.grafo.graphs.model;

import es.urjc.etsii.grafo.solution.Solution;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MSTSolution extends Solution<MSTSolution, MSTInstance> {

    private double score;

    /**
     * Selected vertices, only used by the Minimum Vertex Cover (MVC) demo.
     * Left empty by every other algorithm in this project.
     */
    private final BitSet cover;

    /**
     * Initialize solution from instance
     *
     * @param instance
     */
    public MSTSolution(MSTInstance instance) {
        super(instance);
        score = 0;
        this.cover = new BitSet(instance.v());
    }

    /**
     * Clone constructor
     *
     * @param solution Solution to clone
     */
    public MSTSolution(MSTSolution solution) {
        super(solution);
        this.score = solution.score;
        this.cover = (BitSet) solution.cover.clone();
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

    /**
     * Is the given vertex part of the vertex cover? Only meaningful for the MVC demo.
     * @param vertex vertex id
     * @return true if the vertex is currently selected
     */
    public boolean isInCover(int vertex) {
        return this.cover.get(vertex);
    }

    /**
     * Add a vertex to the vertex cover. Only meaningful for the MVC demo.
     * @param vertex vertex id
     */
    public void addToCover(int vertex) {
        this.cover.set(vertex);
    }

    /**
     * Vertices currently selected as part of the vertex cover. Only meaningful for the MVC demo.
     * @return a new set with the selected vertex ids
     */
    public Set<Integer> getCoverVertices() {
        var result = new HashSet<Integer>();
        for (int v = this.cover.nextSetBit(0); v >= 0; v = this.cover.nextSetBit(v + 1)) {
            result.add(v);
        }
        return result;
    }

    /**
     * Number of vertices currently selected as part of the vertex cover.
     * @return cover size
     */
    public int getCoverSize() {
        return this.cover.cardinality();
    }

    /**
     * Set the solution score to the current vertex cover size. Only meaningful for the MVC demo,
     * as the MVC objective is to minimize the number of selected vertices.
     */
    public void setScoreCover() {
        score = this.cover.cardinality();
    }
}

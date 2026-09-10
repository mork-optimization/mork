package es.urjc.etsii.grafo.cwp.model;

import java.util.Objects;

/**
 * Move that swaps the positions of two vertices in the ordering (the CWP neighbourhood). This is
 * the same operation the custom local searches apply.
 */
public class SwapNodesMove extends CWPBaseMove {

    private final int nodeA;
    private final int nodeB;

    public SwapNodesMove(CWPSolution solution, int nodeA, int nodeB) {
        super(solution);
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.scoreChange = solution.scoreAfterSwap(nodeA, nodeB) - solution.getScore();
    }

    @Override
    protected CWPSolution _execute(CWPSolution solution) {
        solution.swapNodes(nodeA, nodeB);
        return solution;
    }

    @Override
    public String toString() {
        return "Swap(" + nodeA + " <-> " + nodeB + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwapNodesMove that = (SwapNodesMove) o;
        return nodeA == that.nodeA && nodeB == that.nodeB;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeA, nodeB);
    }
}

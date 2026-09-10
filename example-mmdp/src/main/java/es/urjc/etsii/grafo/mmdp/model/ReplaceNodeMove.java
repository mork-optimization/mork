package es.urjc.etsii.grafo.mmdp.model;

import java.util.Objects;

/**
 * Move that replaces a selected node with a non-selected one (the MMDP neighbourhood). This is the
 * same operation the custom local searches apply.
 */
public class ReplaceNodeMove extends MMDPBaseMove {

    private final int oldNode;
    private final int newNode;

    public ReplaceNodeMove(MMDPSolution solution, int oldNode, int newNode) {
        super(solution);
        this.oldNode = oldNode;
        this.newNode = newNode;
        this.scoreChange = solution.scoreAfterReplace(oldNode, newNode) - solution.getScore();
    }

    @Override
    protected MMDPSolution _execute(MMDPSolution solution) {
        solution.replaceNode(oldNode, newNode);
        return solution;
    }

    @Override
    public String toString() {
        return "Replace(" + oldNode + " -> " + newNode + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplaceNodeMove that = (ReplaceNodeMove) o;
        return oldNode == that.oldNode && newNode == that.newNode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldNode, newNode);
    }
}

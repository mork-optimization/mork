package es.urjc.etsii.grafo.mdp.model;

import java.util.Objects;

/**
 * Move that replaces a selected node with a non-selected one (the MDP swap neighbourhood). This is
 * the same operation the hand-written local search applied.
 *
 * <p>The objective delta is computed incrementally in the constructor: removing {@code oldNode} and
 * inserting {@code newNode} changes the total diversity by
 * {@code Σ_{s∈S\{old}} dist(new,s) − Σ_{s∈S\{old}} dist(old,s)}, which
 * {@link MDPSolution#calculateDistanceWithoutNode(MDPNode, MDPNode)} provides for both terms.
 */
public class SwapNodeMove extends MDPBaseMove {

    private final MDPNode oldNode;
    private final MDPNode newNode;

    public SwapNodeMove(MDPSolution solution, MDPNode oldNode, MDPNode newNode) {
        super(solution);
        this.oldNode = oldNode;
        this.newNode = newNode;
        double addContribution = solution.calculateDistanceWithoutNode(oldNode, newNode);
        double removeContribution = solution.calculateDistanceWithoutNode(oldNode, oldNode);
        this.scoreChange = addContribution - removeContribution;
    }

    @Override
    protected MDPSolution _execute(MDPSolution solution) {
        solution.changeNode(oldNode, newNode);
        return solution;
    }

    @Override
    public String toString() {
        return "Swap(" + oldNode + " -> " + newNode + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwapNodeMove that = (SwapNodeMove) o;
        return oldNode.equals(that.oldNode) && newNode.equals(that.newNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldNode, newNode);
    }
}

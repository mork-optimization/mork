package es.urjc.etsii.grafo.cph.model;

import java.util.Objects;

/**
 * Move that replaces an open hub with a node that is not currently a hub (the CPH swap
 * neighbourhood). This is the same operation the custom local searches apply.
 */
public class ChangeHubMove extends CPHBaseMove {

    private final int oldHub;
    private final int newHub;

    public ChangeHubMove(CPHSolution solution, int oldHub, int newHub) {
        super(solution);
        this.oldHub = oldHub;
        this.newHub = newHub;
        this.scoreChange = solution.scoreAfterChangeHub(oldHub, newHub) - solution.getScore();
    }

    @Override
    protected CPHSolution _execute(CPHSolution solution) {
        solution.changeHub(oldHub, newHub);
        return solution;
    }

    @Override
    public String toString() {
        return "ChangeHub(" + oldHub + " -> " + newHub + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeHubMove that = (ChangeHubMove) o;
        return oldHub == that.oldHub && newHub == that.newHub;
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldHub, newHub);
    }
}

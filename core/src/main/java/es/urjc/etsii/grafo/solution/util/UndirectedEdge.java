package es.urjc.etsii.grafo.solution.util;

import java.util.Objects;

/**
 * Undirected edge class for graph problems. Always goes from small number to bigger
 */
public class UndirectedEdge {

    protected final int a, b;

    /**
     * Create a new undirected edge.
     * A and B are REVERSED if (a greater than b), so the following is always true: UndirectedEdge(a,b).equals(UndirectedEdge(b,a))
     *
     * @param a one of the points. Due to the nature of undirected edges, a and b can be swapped when constructing the Edge.
     * @param b the other point.
     */
    public UndirectedEdge(int a, int b) {
        if(a <= b){
            this.a = a;
            this.b = b;
        } else {
            this.a = b;
            this.b = a;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UndirectedEdge that = (UndirectedEdge) o;
        return a == that.a &&
                b == that.b;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    /**
     * <p>Getter for the field <code>a</code>.</p>
     *
     * @return a int.
     */
    public int getA() {
        return a;
    }

    /**
     * <p>Getter for the field <code>b</code>.</p>
     *
     * @return a int.
     */
    public int getB() {
        return b;
    }
}

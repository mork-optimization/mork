package es.urjc.etsii.grafo.mdp.util;

/**
 * A value paired with a numeric weight. Used by {@code MDPSolution} to keep, for every selected
 * node, its contribution to the total diversity (sum of distances to the other selected nodes),
 * which enables the incremental evaluation of add / remove / swap operations.
 */
public class Weighted<E> implements Comparable<Weighted<E>> {

    private E element;
    private double value;

    public Weighted(E element, double value) {
        this.element = element;
        this.value = value;
    }

    public Weighted(Weighted<E> wn) {
        this.element = wn.element;
        this.value = wn.value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Weighted<?> w) {
            return element.equals(w.element) && value == w.value;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return element.hashCode() + 37 * (int) value;
    }

    @Override
    public String toString() {
        return ((element != null) ? element.toString() : "null") + " (" + value + ")";
    }

    public int compareTo(Weighted<E> w) {
        return (this.value > w.value) ? 1 : (this.value < w.value) ? -1 : 0;
    }

    public double getWeight() {
        return value;
    }

    public E getElement() {
        return element;
    }

    public void setElement(E element) {
        this.element = element;
    }

    public void setWeight(double value) {
        this.value = value;
    }

    public static <E> Weighted<E> min(Weighted<E> a, Weighted<E> b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        } else {
            return (a.value < b.value) ? a : b;
        }
    }

    public static <E> Weighted<E> max(Weighted<E> a, Weighted<E> b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        } else {
            return (a.value > b.value) ? a : b;
        }
    }
}

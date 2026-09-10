package es.urjc.etsii.grafo.mdp.util;

import java.util.Iterator;

/**
 * Iterates over the {@code element} of a collection of {@link Weighted} wrappers, so a solution
 * stored as a list of {@code Weighted<MDPNode>} can be iterated as a plain sequence of nodes.
 */
public class WeightedIterator<E> implements Iterator<E> {

    Iterator<Weighted<E>> it;

    public WeightedIterator(Iterator<Weighted<E>> it) {
        this.it = it;
    }

    public boolean hasNext() {
        return it.hasNext();
    }

    public E next() {
        return it.next().getElement();
    }

    public void remove() {
        it.remove();
    }
}

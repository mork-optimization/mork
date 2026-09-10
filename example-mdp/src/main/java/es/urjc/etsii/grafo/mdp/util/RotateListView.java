package es.urjc.etsii.grafo.mdp.util;

import java.util.Iterator;
import java.util.List;

/**
 * A read-only view over a list that iterates every element exactly once but starts each new
 * iteration where the previous one stopped (a rotating cursor). The Tabu-search improver uses it to
 * scan candidate nodes from a different offset each iteration, so it does not always try the same
 * first-improving swap. Reused unchanged from {@code mork-experiments}.
 */
public class RotateListView<T> implements Iterable<T> {

    private class IteratorRotate implements Iterator<T> {

        private final int initialCounter;
        private boolean hasNext = true;

        public IteratorRotate() {
            initialCounter = actualCounter;
        }

        public boolean hasNext() {
            return hasNext;
        }

        public T next() {
            T elem = list.get(actualCounter);
            actualCounter = (actualCounter + 1) % list.size();
            hasNext = (actualCounter != initialCounter);
            return elem;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final List<? extends T> list;
    int actualCounter;

    public RotateListView(List<? extends T> list) {
        this.list = list;
    }

    public Iterator<T> iterator() {
        return new IteratorRotate();
    }
}

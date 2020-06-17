package es.urjc.etsii.grafo.solution;

import java.util.Comparator;

public abstract class MoveComparator<M extends Move<?,?>> implements Comparator<M> {

    @Override
    public int compare(M m1, M m2) {
        boolean bestA = getBestMove(m1, m2) == m1;
        boolean bestB = getBestMove(m2, m1) == m2;
        //System.out.format("\tDEBUG: %s, %s, %s, %s\n", a.getValue(), b.getValue(), bestA, bestB);

        assert bestA || bestB;
        if(bestA && bestB)  return 0;
        if(bestA)           return -1;
        else                return 1;
    }

    /**
     * Returns the best of two different movements
     * @param m1 first move
     * @param m2 second move
     * @return best move of two
     */
    public abstract M getBestMove(M m1, M m2);
}
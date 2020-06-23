package es.urjc.etsii.grafo.solution;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public abstract class MoveComparator<M extends Move<?,?>> implements Comparator<M> {

    @Override
    public int compare(M m1, M m2) {
        Optional<M> move = getStrictBestMove(m1, m2);
        if(move.isEmpty()) return 0;
        if(move.get() == m1) return -1;
        if(move.get() == m2) return 1;
        throw new IllegalStateException("getBestMove() must return one of the argument moves or an empty Optional");
    }

    /**
     * Returns the best of two different movements
     * @param m1 first move
     * @param m2 second move
     * @return best move of two. If they are equals, return any of them
     *
     */
    public M getBest(M m1, M m2){
        var result = getStrictBestMove(m1, m2);
        return result.orElse(m1);
    }

    /**
     * Returns the best of two different movements
     * @param m1 first move
     * @param m2 second move
     * @return best move of two. Empty optional if both moves are equal
     */
    public abstract Optional<M> getStrictBestMove(M m1, M m2);

    /**
     * Validate a MoveComparator implementation against a Moves candidate list
     * @param comparator Comparator implementation to test
     * @param cl Move candidate list to test against. Checks all combinations --> O(N^3) complexity.
     * @return Always returns true. Throws AssertionError if the validation fails.
     */
    public static <M> boolean validateComparator(Comparator<M> comparator, List<M> cl) {
        for (int i = 0; i < cl.size(); i++) {
            M x = cl.get(i);
            for (int j = 0; j < cl.size(); j++) {
                M y = cl.get(j);
                assert comparator.compare(x, y) == -comparator.compare(y, x);
                for (int k = 0; k < cl.size(); k++) {
                    M z = cl.get(k);
                    /*  From the Java docs:
                        The implementor must ensure that sgn(compare(x, y)) == -sgn(compare(y, x)) for all x and y.
                        (This implies that compare(x, y) must throw an exception if and only if compare(y, x) throws an exception.)
                        The implementor must also ensure that the relation is transitive:
                        ((compare(x, y)>0) && (compare(y, z)>0)) implies compare(x, z)>0.
                        Finally, the implementor must ensure that compare(x, y)==0 implies that
                        sgn(compare(x, z))==sgn(compare(y, z)) for all z.
                    */
                    if(comparator.compare(x,y) == 0){
                        assert comparator.compare(x, z) == comparator.compare(y, z): x.toString() + y.toString() + z.toString() + "Comparison: "+comparator.compare(x,y) + ","+comparator.compare(y,z) + "," + comparator.compare(x,z);
                    }
                    if(comparator.compare(x,y) == 1 && comparator.compare(y,z) == 1){
                        assert comparator.compare(x, z) == 1: x.toString() + y.toString() + z.toString() + "Comparison: "+comparator.compare(x,y) + ","+comparator.compare(y,z) + "," + comparator.compare(x,z);
                    }
                    if(comparator.compare(x,y) == -1 && comparator.compare(y,z) == -1){
                        assert comparator.compare(x, z) == -1: x.toString() + y.toString() + z.toString() + "Comparison: "+comparator.compare(x,y) + ","+comparator.compare(y,z) + "," + comparator.compare(x,z);
                    }
                }
            }
        }

        return true;
    }
}
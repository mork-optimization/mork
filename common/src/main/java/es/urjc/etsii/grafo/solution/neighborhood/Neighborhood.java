package es.urjc.etsii.grafo.solution.neighborhood;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Defines a neighbourhood.
 * A neighborhoods represents all potential solutions that can be reached for a given solution applying a given move.
 * Usually used inside, but not limited to, a local search procedure,
 */
public abstract class Neighborhood<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> {

    /**
     * Build an exhaustive stream that allows iterating the whole neighborhood
     * Using a stream is more efficient that a list
     * as moves are only generated if they are needed
     *
     * @param s Solution used to generate the neighborhood
     * @return Stream with all the available moves in the neighborhood
     */
    public abstract Stream<M> stream(S s);

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }


    /**
     * Concatenate several neighborhoods, such as N1(A,B,C) and N2(D,E,F) return a new neighborhood with moves N(A,B,C,D,E,F)
     * @param neighborhoods neighborhoods to concatenate
     * @return a new neighborhood which returns the moves from each neighborhood as a sequence
     * @param <M> Move type
     * @param <S> Solution type
     * @param <I> Instance type
     */
    @SafeVarargs
    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> Neighborhood<M,S,I> concat(Neighborhood<M,S,I>... neighborhoods){
        return new ConcatNeighborhood<>(neighborhoods);
    }

    /**
     * Alternate between several neighborhoods, such as N1(A,B,C) and N2(D,E,F) return a new neighborhood with moves N(A,D,B,E,C,F)
     * @param neighborhoods neighborhoods to alternate
     * @return a new neighborhood which returns the moves from each neighborhood alternating between them
     * @param <M> Move type
     * @param <S> Solution type
     * @param <I> Instance type
     */
    @SafeVarargs
    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> Neighborhood<M,S,I> interleave(Neighborhood<M,S,I>... neighborhoods){
        return new InterleavedNeighborhood<>(neighborhoods);
    }

    /**
     * Create an empty neighborhood with no moves
     * @return empty neighborhood
     * @param <M> Move type
     * @param <S> Solution type
     * @param <I> Instance type
     */
    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> Neighborhood<M,S,I> empty(){
        return new EmptyNeighborhood<>();
    }

    private static class EmptyNeighborhood<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> extends Neighborhood<M,S,I> {
        @Override
        public Stream<M> stream(S s) {
            return Stream.empty();
        }

        @Override
        public String toString() {
            return "EmptyNeighborhood{}";
        }
    }

    private static class ConcatNeighborhood<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> extends Neighborhood<M,S,I> {
        private final Neighborhood<M, S, I>[] neighborhoods;

        @SafeVarargs
        private ConcatNeighborhood(Neighborhood<M, S, I>... neighborhoods) {
            this.neighborhoods = Objects.requireNonNull(neighborhoods);
        }

        @Override
        public Stream<M> stream(S s) {
            if(neighborhoods.length == 0){
                return Stream.empty();
            }
            Stream<M> stream = neighborhoods[0].stream(s);
            for (int i = 1; i < neighborhoods.length; i++) {
                stream = Stream.concat(stream, neighborhoods[i].stream(s));
            }
            return stream;
        }
    }

    private static class InterleavedNeighborhood<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> extends Neighborhood<M,S,I> {

        private final Neighborhood<M,S,I>[] neighborhoods;

        @SafeVarargs
        private InterleavedNeighborhood(Neighborhood<M, S, I>... neighborhoods) {
            this.neighborhoods = neighborhoods;
        }

        @Override
        public Stream<M> stream(S solution) {
            Spliterator<? extends M>[] sps = new Spliterator[this.neighborhoods.length];
            for (int i = 0; i < neighborhoods.length; i++) {
                sps[i] = neighborhoods[i].stream(solution).spliterator();
            }
            long size = 0;
            int ch = (Spliterator.NONNULL | Spliterator.SIZED);
            for (Spliterator<? extends M> sp : sps) {
                size += sp.estimateSize();
                ch &= sp.characteristics();
            }
            if(size < 0) {
                size = Long.MAX_VALUE;
            }

            return StreamSupport.stream(new Spliterators.AbstractSpliterator<>(size, ch) {
                List<Spliterator<? extends M>> spliterators = new ArrayList<>(Arrays.asList(sps));
                int lastIndex = 0;

                @Override
                public boolean tryAdvance(Consumer<? super M> action) {
                    while (true) {
                        var target = spliterators.get(lastIndex);
                        if(!target.tryAdvance(action)){
                            // current neighborhood ended, remove
                            spliterators.remove(target);
                            if(spliterators.isEmpty()){
                                // All neighborhoods ended
                                return false;
                            } else {
                                // If we have removed the last neighborhood, next is the first one
                                lastIndex %= spliterators.size();
                            }
                        } else {
                            // Success
                            lastIndex = (lastIndex + 1) % spliterators.size();
                            return true;
                        }
                    }
                }
            }, false);
        }
    }
}

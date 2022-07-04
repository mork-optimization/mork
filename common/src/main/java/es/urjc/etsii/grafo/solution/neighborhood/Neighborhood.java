package es.urjc.etsii.grafo.solution.neighborhood;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.LazyMove;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Defines a neighbourhood.
 * A neighborhoods represents all potential solutions that can be reached for a given solution applying a given move.
 * Usually used inside, but not limited to, a local search procedure,
 */
public abstract class Neighborhood<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> {

    public static final int UNKNOWN_SIZE = Integer.MAX_VALUE; // Consistency with Spliterator API

    /**
     * <p>
     * Optionally calculate how big the neighborhood is for a given solution.
     * It does not need to be an exact value, but it should be an upper bound.
     * It is perfectly valid to estimate the size to 100 and then returning only 90 elements from the neighborhood,
     * but it is not ok to estimate size to 10 and then returning 100 elements.
     * Internally will be used to correctly size data structures and try to improve performance.
     * Implementation  should have O(1) or O(log n) complexity, if the size can be calculated but takes O(n) or longer, return {@link Neighborhood#UNKNOWN_SIZE} instead.
     * </p>
     *
     * @param moves stream of moves
     * @param size  neighborhood size if known, {@link Neighborhood#UNKNOWN_SIZE} if not
     * @param <M>   Move type
     * @param <S>   Solution type
     * @param <I>   Instance type
     */
    public record ExploreResult<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance>(Stream<M> moves,
                                                                                                    int size) {

        /**
         * Empty explore results
         */
        public ExploreResult(){
            this(Stream.empty(), 0);
        }

        /**
         * Unknown size constructor from stream
         *
         * @param moves move stream
         */
        public ExploreResult(Stream<M> moves) {
            this(moves, UNKNOWN_SIZE);
        }

        /**
         * Explore result from a LazyMove, unknown neighborhood size
         *
         * @param move move list
         */
        public ExploreResult(LazyMove<S, I> move) {
            this(move, UNKNOWN_SIZE);
        }

        /**
         * Explore result from a LazyMove, when the neighborhood size is known.
         *
         * @param move move list
         * @param size upperbound neighborhood size for the current solution
         */
        public ExploreResult(LazyMove<S, I> move, int size) {
            // TODO: review ugly casting
            this((Stream<M>) Stream.iterate(move, Objects::nonNull, (LazyMove<S, I> m) -> m.next()), size);
        }

        /**
         * Explore result from a list of moves (eager exploration)
         *
         * @param moves move list
         */
        public ExploreResult(List<M> moves) {
            this(moves.stream(), moves.size());
        }

        /**
         * Optionally calculate how big the neighborhood is for a given solution.
         * It does not need to be an exact value, but it should be an upper bound.
         * It is perfectly valid to estimate the size to 100 and then returning only 90 elements from the neighborhood,
         * but it is not ok to estimate size to 10 and then returning 100 elements.
         * Internally will be used to correctly size data structures and try to improve performance.
         * Implementation  should have O(1) or O(log n) complexity, if the size can be calculated but takes O(n) or longer, return {@link Neighborhood#UNKNOWN_SIZE} instead.
         *
         * @return true if the neighborhood has a size estimation, UNKNOWN_SIZE if the size is unknown or cannot be estimated.
         */
        public boolean sized() {
            return this.size != UNKNOWN_SIZE;
        }
    }

    /**
     * Build an exhaustive stream that allows iterating the whole neighborhood
     * Using a stream is more efficient that a list
     * as moves are only generated if they are needed
     *
     * @param s Solution used to generate the neighborhood
     * @return Stream with all the available moves in the neighborhood
     */
    public abstract ExploreResult<M, S, I> explore(S s);

    /**
     * <p>
     * Optionally calculate how big the neighborhood is for a given solution.
     * It does not need to be an exact value, but it should be an upper bound.
     * It is perfectly valid to estimate the size to 100 and then returning only 90 elements from the neighborhood,
     * but it is not ok to estimate size to 10 and then returning 100 elements.
     * Internally will be used to correctly size data structures and try to improve performance.
     * Moreover, can be used when using a random neighborhood to optionally balance probabilities,
     * so the bigger neighborhood has a bigger chance of having a move picked, instead of equal probability between all neighborhoods.
     * Implementation  should have O(1) or O(log n) complexity, if the size can be calculated but takes O(n) or longer, it is recommended to return {@link Neighborhood#UNKNOWN_SIZE} instead.
     * </p>
     */
    public int neighborhoodSize(S s) {
        return Neighborhood.UNKNOWN_SIZE;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }


    /**
     * Concatenate several neighborhoods, such as N1(A,B,C) and N2(D,E,F) return a new neighborhood with moves N(A,B,C,D,E,F)
     *
     * @param neighborhoods neighborhoods to concatenate
     * @param <M>           Move type
     * @param <S>           Solution type
     * @param <I>           Instance type
     * @return a new neighborhood which returns the moves from each neighborhood as a sequence
     */
    @SafeVarargs
    public static <M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> Neighborhood<M, S, I> concat(Neighborhood<M, S, I>... neighborhoods) {
        return new ConcatNeighborhood<>(neighborhoods);
    }

    /**
     * Alternate between several neighborhoods, such as N1(A,B,C) and N2(D,E,F) return a new neighborhood with moves N(A,D,B,E,C,F)
     *
     * @param neighborhoods neighborhoods to alternate
     * @param <M>           Move type
     * @param <S>           Solution type
     * @param <I>           Instance type
     * @return a new neighborhood which returns the moves from each neighborhood alternating between them
     */
    @SafeVarargs
    public static <M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> Neighborhood<M, S, I> interleave(Neighborhood<M, S, I>... neighborhoods) {
        return new InterleavedNeighborhood<>(neighborhoods);
    }

    /**
     * Create an empty neighborhood with no moves
     *
     * @param <M> Move type
     * @param <S> Solution type
     * @param <I> Instance type
     * @return empty neighborhood
     */
    public static <M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> Neighborhood<M, S, I> empty() {
        return new EmptyNeighborhood<>();
    }

    private static class EmptyNeighborhood<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> extends Neighborhood<M, S, I> {

        @Override
        public ExploreResult<M, S, I> explore(S s) {
            return new ExploreResult<>(Stream.empty(), 0);
        }

        @Override
        public String toString() {
            return "EmptyNeighborhood{}";
        }
    }


    private abstract static class RandomFromNeighborhood<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> extends RandomizableNeighborhood<M, S, I> {

        private final boolean balanceProbabilities;
        final RandomizableNeighborhood<M, S, I>[] neighborhoods;

        /**
         * Pick a random move from the given neighborhoods
         *
         * @param balanceProbabilities if false, a move will be picked with equal probability from each neighborhood.
         *                             if true, probability depends on the neighborhood size if available.
         * @param neighborhoods        list of neighborhoods where we can choose random moves from
         */
        @SafeVarargs
        protected RandomFromNeighborhood(boolean balanceProbabilities, RandomizableNeighborhood<M, S, I>... neighborhoods) {
            this.balanceProbabilities = balanceProbabilities;
            this.neighborhoods = neighborhoods;
        }

        @Override
        public int neighborhoodSize(S s) {
            int totalSize = 0;
            boolean sized = true;
            for (var neighborhood : neighborhoods) {
                var partialResult = neighborhood.explore(s);
                sized &= partialResult.sized();
                totalSize += partialResult.size;
            }
            return sized ? totalSize : UNKNOWN_SIZE;
        }

        @Override
        public Optional<M> getRandomMove(S solution) {
            return this.balanceProbabilities ?
                    balancedPick(solution) :
                    equalProbPick(solution);
        }

        private Optional<M> equalProbPick(S solution) {
            var r = RandomManager.getRandom();
            int chosenNeighborhood = r.nextInt(this.neighborhoods.length);
            return this.neighborhoods[chosenNeighborhood].getRandomMove(solution);
        }

        private Optional<M> balancedPick(S solution) {
            var r = RandomManager.getRandom();
            int totalSize = 0;
            int[] sizes = new int[this.neighborhoods.length];
            for (int i = 0; i < neighborhoods.length; i++) {
                RandomizableNeighborhood<M, S, I> neighborhood = neighborhoods[i];
                var size = neighborhood.neighborhoodSize(solution);
                if (size == UNKNOWN_SIZE) {
                    throw new IllegalArgumentException(String.format("Cannot make a balanced pick, neighborhood %s did not estimate its size. Override method ", neighborhood.getClass().getSimpleName()));
                }
                sizes[i] = size;
                totalSize += size;
            }

            int pick = r.nextInt(totalSize);
            int chosenNeighborhood = 0;
            while (pick >= sizes[chosenNeighborhood]) {
                pick -= sizes[chosenNeighborhood];
                chosenNeighborhood++;
            }

            return this.neighborhoods[chosenNeighborhood].getRandomMove(solution);

        }
    }

    private abstract static class DerivedNeighborhood<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> extends Neighborhood<M, S, I> {
        final Neighborhood<M, S, I>[] neighborhoods;

        @SafeVarargs
        public DerivedNeighborhood(Neighborhood<M, S, I>... neighborhoods) {
            this.neighborhoods = Objects.requireNonNull(neighborhoods);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" +
                    "neighs=" + Arrays.toString(this.neighborhoods) +
                    "}";
        }
    }

    private static class ConcatNeighborhood<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> extends DerivedNeighborhood<M, S, I> {

        @SafeVarargs
        private ConcatNeighborhood(Neighborhood<M, S, I>... neighborhoods) {
            super(neighborhoods);
        }

        @Override
        public ExploreResult<M, S, I> explore(S s) {
            if (neighborhoods.length == 0) {
                return new ExploreResult<>(Stream.empty(), 0);
            }
            int totalSize = 0;
            boolean sized = true;
            Stream<M> stream = null;
            for (var neighborhood : this.neighborhoods) {
                var partialResult = neighborhood.explore(s);
                sized &= partialResult.sized();
                totalSize += partialResult.size;
                if (stream == null) {
                    stream = partialResult.moves;
                } else {
                    stream = Stream.concat(stream, partialResult.moves);
                }
            }

            return new ExploreResult<>(stream, sized ? totalSize : UNKNOWN_SIZE);
        }
    }

    private static class InterleavedNeighborhood<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> extends DerivedNeighborhood<M, S, I> {

        @SafeVarargs
        private InterleavedNeighborhood(Neighborhood<M, S, I>... neighborhoods) {
            super(neighborhoods);
        }

        @Override
        public ExploreResult<M, S, I> explore(S solution) {

            int totalSize = 0;
            boolean sized = true;
            var sps = new ArrayList<Spliterator<? extends M>>(this.neighborhoods.length);

            for (var neighborhood : neighborhoods) {
                var partialResult = neighborhood.explore(solution);
                sps.add(partialResult.moves().spliterator());
                sized &= partialResult.sized();
                totalSize += partialResult.size();
            }

            int ch = Spliterator.NONNULL;
            if (sized) {
                ch |= Spliterator.SIZED;
            } else {
                totalSize = UNKNOWN_SIZE;
            }

            return new ExploreResult<>(StreamSupport.stream(new Spliterators.AbstractSpliterator<>(totalSize, ch) {
                List<Spliterator<? extends M>> spliterators = sps;
                int lastIndex = 0;

                @Override
                public boolean tryAdvance(Consumer<? super M> action) {
                    while (true) {
                        var target = spliterators.get(lastIndex);
                        if (!target.tryAdvance(action)) {
                            // current neighborhood ended, remove
                            spliterators.remove(target);
                            if (spliterators.isEmpty()) {
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
            }, false), totalSize);
        }
    }
}

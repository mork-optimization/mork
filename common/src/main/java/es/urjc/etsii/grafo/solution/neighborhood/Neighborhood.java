package es.urjc.etsii.grafo.solution.neighborhood;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.io.Instance;
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
@AlgorithmComponent
public abstract class Neighborhood<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> {

    public static final int UNKNOWN_SIZE = Integer.MAX_VALUE; // Consistency with Spliterator API

    /**
     * Build an exhaustive stream that allows iterating the whole neighborhood
     * Using a stream is more efficient that a list
     * as moves are only generated if they are needed
     *
     * @param solution Solution used to generate the neighborhood
     * @return Stream with all the available moves in the neighborhood
     */
    public abstract ExploreResult<M, S, I> explore(S solution);

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
    public int neighborhoodSize(S solution) {
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

    /**
     * Create a neighborhood that picks random moves from a set of given neighborhoods
     * @param balanced if false, all neighborhoods will be chosen with the same probability. If true, the neighborhood
     *                 size for the current solution will be taken into account
     * @return a RandomizableNeighborhood that randomly picks movements from a given set of neighborhoods.
     * @param <M> Move type
     * @param <S> Solution type
     * @param <I> Instance type
     */
    public static <M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> RandomizableNeighborhood<M, S, I> random(boolean balanced, RandomizableNeighborhood<M, S, I>... neighborhoods) {
        return new RandomFromNeighborhood<>(balanced, neighborhoods);
    }

    public static class EmptyNeighborhood<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> extends RandomizableNeighborhood<M, S, I> {

        public EmptyNeighborhood() {}

        @Override
        public ExploreResult<M, S, I> explore(S solution) {
            return new ExploreResult<>(Stream.empty(), 0);
        }

        @Override
        public String toString() {
            return "EmptyNeighborhood{}";
        }

        @Override
        public Optional<M> getRandomMove(S solution) {
            return Optional.empty();
        }
    }


    private static class RandomFromNeighborhood<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> extends RandomizableNeighborhood<M, S, I> {

        private final boolean balanceProbabilities;
        private final RandomizableNeighborhood<M, S, I>[] neighborhoods;
        private final Neighborhood<M, S, I> neighborhoodForExplore;

        /**
         * Pick a random move from the given neighborhoods
         *
         * @param balanceProbabilities if false, a move will be picked with equal probability from each neighborhood.
         *                             if true, probability depends on the neighborhood size if available.
         * @param neighborhoods        list of neighborhoods where we can choose random moves from
         */
        protected RandomFromNeighborhood(boolean balanceProbabilities, RandomizableNeighborhood<M, S, I>[] neighborhoods) {
            this.balanceProbabilities = balanceProbabilities;
            this.neighborhoods = neighborhoods;
            this.neighborhoodForExplore = Neighborhood.concat(neighborhoods);
        }

        @Override
        public ExploreResult<M, S, I> explore(S solution) {
            return this.neighborhoodForExplore.explore(solution);
        }

        @Override
        public int neighborhoodSize(S solution) {
            int totalSize = 0;
            boolean sized = true;
            for (var neighborhood : neighborhoods) {
                var partialResult = neighborhood.explore(solution);
                sized &= partialResult.sized();
                totalSize += partialResult.size();
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
        public ExploreResult<M, S, I> explore(S solution) {
            if (neighborhoods.length == 0) {
                return new ExploreResult<>(Stream.empty(), 0);
            }
            int totalSize = 0;
            boolean sized = true;
            Stream<M> stream = null;
            for (var neighborhood : this.neighborhoods) {
                var partialResult = neighborhood.explore(solution);
                sized &= partialResult.sized();
                totalSize += partialResult.size();
                if (stream == null) {
                    stream = partialResult.moves();
                } else {
                    stream = Stream.concat(stream, partialResult.moves());
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

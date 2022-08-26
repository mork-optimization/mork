package es.urjc.etsii.grafo.solution.neighborhood;


import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.LazyMove;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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
 * @param <M>   Move type
 * @param <S>   Solution type
 * @param <I>   Instance type
 */
public class ExploreResult<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> {
    private final Stream<M> moves;
    private final int size;

    /**
     * Unknown size constructor from stream
     *
     * @param moves move stream
     */
    public static <M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> ExploreResult<M,S,I> fromStream(Stream<M> moves){
        return fromStream(moves, Neighborhood.UNKNOWN_SIZE);
    }

    /**
     * Explore result from a stream of moves, with given size.
     * @param moves stream of moves
     * @param size neighborhood size if known, {@link Neighborhood#UNKNOWN_SIZE} if not
     */
    public static <M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> ExploreResult<M,S,I> fromStream(Stream<M> moves, int size){
        return new ExploreResult<>(moves, size);
    }

    /**
     * Empty explore results
     * @return empty explore results
     */
    public static <M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> ExploreResult<M,S,I> empty() {
        return fromStream(Stream.empty(), 0);
    }

    /**
     * Explore result from a LazyMove, when the neighborhood size is known.
     *
     * @param move move list
     * @param size upperbound neighborhood size for the current solution
     */
    public static <M extends LazyMove<S, I>, S extends Solution<S, I>, I extends Instance> ExploreResult<M,S,I> fromLazyMove(S solution, M move, int size){
        return new ExploreResult<>(
                Stream.iterate(move, Objects::nonNull, m -> (M) m.next(solution)),
                size
        );
    }

    /**
     * Explore result from a LazyMove, when the neighborhood size is unknown.
     *
     * @param solution current solution
     * @param move move
     */
    public static <M extends LazyMove<S, I>, S extends Solution<S, I>, I extends Instance> ExploreResult<M,S,I> fromLazyMove(S solution, M move){
        return fromLazyMove(solution, move, Neighborhood.UNKNOWN_SIZE);
    }

    /**
     * Explore result from a list
     *
     * @param moves list of moves
     */
    public static <M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> ExploreResult<M,S,I> fromList(List<M> moves){
        return new ListExploreResult<>(moves);
    }

    /**
     * Explore result from a stream of moves, with given size.
     * @param moves stream of moves
     * @param size  neighborhood size if known, {@link Neighborhood#UNKNOWN_SIZE} if not
     */
    public ExploreResult(Stream<M> moves, int size) {
        this.moves = moves;
        this.size = size;
    }

    /**
     * Empty explore results
     */
    public ExploreResult() {
        this(Stream.empty(), 0);
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
        return this.size != Neighborhood.UNKNOWN_SIZE;
    }

    public Stream<M> moves() {
        return moves;
    }

    public int size() {
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ExploreResult) obj;
        return Objects.equals(this.moves, that.moves) &&
                this.size == that.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(moves, size);
    }

    @Override
    public String toString() {
        return "ExploreResult[" +
                "moves=" + moves + ", " +
                "size=" + size + ']';
    }
}

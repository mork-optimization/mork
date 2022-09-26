package es.urjc.etsii.grafo.shake;


import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Different ways to shake a solution, RandomShake for a reference implementation
 *
 * @param <S> Solution class
 * @param <I> Instance class
 * @see RandomMoveShake
 */
@AlgorithmComponent
public abstract class Shake<S extends Solution<S,I>, I extends Instance> {

    /**
     * Shake the solution. Use k to calculate how powerful the shake should be in your implementation.
     * Can be as simple as number of elements to remove, or to swap. Whatever you want.
     *
     * @param solution Solution to shake
     * @param k shake strength
     * @return shaken solution. Shaken, not stirred.
     */
    public abstract S shake(S solution, int k);

    /**
     * Create a no operation shake method
     * Returns the solution immediately without executing any operation
     * @param <S> Solution class
     * @param <I> Instance class
     * @return Null shake method
     */
    public static <S extends Solution<S,I>, I extends Instance> Shake<S,I> nul(){
        return new NullShake<>();
    }

    /**
     * Do nothing shake
     *
     * @param <S> Solution class
     * @param <I> Instance class
     */
    public static class NullShake<S extends Solution<S,I>,I extends Instance> extends Shake<S,I> {
        @AutoconfigConstructor
        public NullShake() {}

        @Override
        public S shake(S solution, int k) {
            return solution;
        }
    }
}

package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.create.NoOPConstructive;
import es.urjc.etsii.grafo.solver.destructor.Shake;
import es.urjc.etsii.grafo.solver.improve.Improver;

/**
 * Create NO-OPERATION algorithm components
 * Return solution as is without making any change.
 */
public class NoOp {

    /**
     * Create no operation constructive method
     *
     * @param <S> Solution class
     * @param <I> Instance class
     * @return NoOp constructive
     */
    public static <S extends Solution<S,I>, I extends Instance> Constructive<S,I> constructive(){
        return new NoOPConstructive<>();
    }

    /**
     * Create no operation improve method
     *
     * @param <S> Solution class
     * @param <I> Instance class
     * @return NoOp improve method
     */
    public static <S extends Solution<S,I>, I extends Instance> Improver<S,I> improver(){
        return new NoOpImprover<>();
    }

    /**
     * Create no operation improve method
     *
     * @param <S> Solution class
     * @param <I> Instance class
     * @return NoOp improve method
     */
    public static <S extends Solution<S,I>, I extends Instance> Shake<S,I> shake(){
        return new NoOpShake<>();
    }

    /**
     * Do nothing constructive
     *
     * @param <S> Solution class
     * @param <I> Instance class
     */
    public static class NoOpConstructive<S extends Solution<S,I>,I extends Instance> extends Constructive<S,I> {
        @Override
        public S construct(S s) {
            return s;
        }
    }

    /**
     * Do nothing local search
     *
     * @param <S> Solution class
     * @param <I> Instance class
     */
    public static class NoOpImprover<S extends Solution<S,I>,I extends Instance> extends Improver<S,I> {
        @Override
        protected S _improve(S s) {
            return s;
        }
    }

    /**
     * Do nothing shake
     *
     * @param <S> Solution class
     * @param <I> Instance class
     */
    public static class NoOpShake<S extends Solution<S,I>,I extends Instance> extends Shake<S,I> {
        @Override
        public S shake(S s, int k) {
            return s;
        }
    }
}

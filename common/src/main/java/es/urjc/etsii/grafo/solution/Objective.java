package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.io.Instance;

import java.util.function.ToDoubleFunction;

/**
 * Represents an objective function to be optimized for a given problem. In general, an objective function is a function that takes a solution and returns a double value representing the quality of the solution.
 * In this class, we also include a method to evaluate the quality of a move, which is the difference or delta in the objective function value between the solution before applying the move and the solution after applying the move.
 * Objective functions can be of two types: MINIMIZE or MAXIMIZE. It is important to correctly set the type of the objective function, as it will affect the behavior of the algorithms.
 * In single objective optimization, usually there is a single objective to optimize. However, sometimes it can be useful to use secondary functions, for example when the solution landscape is flat.
 * Usage of objectives is left to the user discretion.
 * In multi-objective optimization, of course there are multiple objective functions.
 * @param <S> Solution class
 * @param <I> Instance class
 */
public abstract class Objective<S extends Solution<S,I>, I extends Instance> {

    public static <S extends Solution<S,I>, I extends Instance> Objective<S,I> ofMinimizing(ToDoubleFunction<S> evaluateSolution, ToDoubleFunction<Move<S,I>> evaluateMove){
        return new SimpleObjective<>(FMode.MINIMIZE, evaluateSolution, evaluateMove);
    }

    public static <S extends Solution<S,I>, I extends Instance> Objective<S,I> ofMaximizing(ToDoubleFunction<S> evaluateSolution, ToDoubleFunction<Move<S,I>> evaluateMove){
        return new SimpleObjective<>(FMode.MAXIMIZE, evaluateSolution, evaluateMove);
    }

    public static <S extends Solution<S,I>, I extends Instance> Objective<S,I> of(FMode fMode, ToDoubleFunction<S> evaluateSolution, ToDoubleFunction<Move<S,I>> evaluateMove){
        return new SimpleObjective<>(fMode, evaluateSolution, evaluateMove);
    }

    public abstract double evaluate(S solution);
    public abstract double evaluate(Move<S,I> move);
    public abstract FMode getFMode();
    public abstract String getName();


    public static class SimpleObjective<S extends Solution<S,I>, I extends Instance> extends Objective<S, I> {

        private final ToDoubleFunction<S> evaluateSolution;
        private final ToDoubleFunction<Move<S,I>> evaluateMove;
        private final FMode fMode;

        public SimpleObjective(FMode fMode, ToDoubleFunction<S> evaluateSolution, ToDoubleFunction<Move<S, I>> evaluateMove) {
            this.evaluateSolution = evaluateSolution;
            this.evaluateMove = evaluateMove;
            this.fMode = fMode;
        }

        @Override
        public double evaluate(S solution) {
            return this.evaluateSolution.applyAsDouble(solution);
        }

        @Override
        public double evaluate(Move<S,I> move) {
            return this.evaluateMove.applyAsDouble(move);
        }

        @Override
        public FMode getFMode() {
            return fMode;
        }

        @Override
        public String getName() {
            return this.getClass().getSimpleName();
        }
    }
}

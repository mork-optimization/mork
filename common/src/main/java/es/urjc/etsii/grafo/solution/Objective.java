package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.io.Instance;

import java.util.Comparator;
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
public abstract class Objective<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> {

    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> Objective<M,S,I> ofMinimizing(String name, ToDoubleFunction<S> evaluateSolution, ToDoubleFunction<M> evaluateMove){
        return new SimpleObjective<>(name, FMode.MINIMIZE, evaluateSolution, evaluateMove);
    }

    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> Objective<M,S,I> ofMaximizing(String name, ToDoubleFunction<S> evaluateSolution, ToDoubleFunction<M> evaluateMove){
        return new SimpleObjective<>(name, FMode.MAXIMIZE, evaluateSolution, evaluateMove);
    }

    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> Objective<M,S,I> of(String name, FMode fMode, ToDoubleFunction<S> evaluateSolution, ToDoubleFunction<M> evaluateMove){
        return new SimpleObjective<>(name, fMode, evaluateSolution, evaluateMove);
    }

    public abstract double evalSol(S solution);
    public abstract double evalMove(M move);
    public abstract FMode getFMode();
    public abstract String getName();

    public boolean isBetterOrEquals(S a, double b){
        return getFMode().isBetterOrEqual(evalSol(a), b);
    }

    public boolean isBetterOrEqual(S a, S b){
        return getFMode().isBetterOrEqual(evalSol(a), evalSol(b));
    }

    public boolean isBetterOrEqual(double a, double b){
        return getFMode().isBetterOrEqual(a, b);
    }

    public boolean isBetter(S a, double b){
        return isBetter(evalSol(a), b);
    }

    public boolean isBetter(S a, S b){
        return isBetter(evalSol(a), evalSol(b));
    }

    public boolean isBetter(double a, double b){
        return getFMode().isBetter(a, b);
    }

    public double getBadValue(){
        return getFMode().getBadValue();
    }

    public boolean improves(double a){
        return getFMode().improves(a);
    }

    /**
     * Comparator that orders solutions by quality
     * @return a comparator that sorts solutions from best to worse
     */
    public Comparator<S> comparator() {
        Comparator<S> c = Comparator.comparingDouble(this::evalSol);
        if (this.getFMode() == FMode.MINIMIZE) {
            return c; // Default is ascending order
        }
        return c.reversed();
    }

    /**
     * Comparator that orders moves by quality
     * @return a comparator that sorts moves from best to worse
     */
    public Comparator<? super Move<S,I>> comparatorMove() {
        Comparator<Move<S,I>> c = Comparator.comparingDouble(m -> this.evalMove((M) m));
        if (this.getFMode() == FMode.MINIMIZE) {
            return c; // Default is ascending order
        }
        return c.reversed();
    }

    public S bestSolution(Iterable<S> list){
        S best = null;
        double bestScore = Double.NaN;
        for(var solution: list){
            if(best == null){
                best = solution;
                bestScore = evalSol(solution);
            } else {
                double currentScore = evalSol(solution);
                if(isBetter(currentScore, bestScore)){
                    best = solution;
                    bestScore = currentScore;
                }
            }
        }
        return best;
    }


    public M bestMove(Iterable<M> list){
        M best = null;
        double bestScore = Double.NaN;
        for(var move: list){
            if(best == null){
                best = move;
                bestScore = evalMove(move);
            } else {
                double currentScore = evalMove(move);
                if(isBetter(currentScore, bestScore)){
                    best = move;
                    bestScore = currentScore;
                }
            }
        }
        return best;
    }

    public double best(double a, double b){
        return getFMode().best(a, b);
    }

    public M bestMove(M m1, M m2){
        double score1 = evalMove(m1);
        double score2 = evalMove(m2);
        return isBetter(score2, score1) ? m2 : m1;
    }

    public S bestSolution(S s1, S s2){
        double score1 = evalSol(s1);
        double score2 = evalSol(s2);
        return isBetter(score2, score1) ? s2 : s1;
    }

    public static class SimpleObjective<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> extends Objective<M, S, I> {

        private final ToDoubleFunction<S> evaluateSolution;
        private final ToDoubleFunction<M> evaluateMove;
        private final FMode fMode;
        private final String name;

        public SimpleObjective(String name, FMode fMode, ToDoubleFunction<S> evaluateSolution, ToDoubleFunction<M> evaluateMove) {
            this.name = name;
            this.evaluateSolution = evaluateSolution;
            this.evaluateMove = evaluateMove;
            this.fMode = fMode;
        }

        @Override
        public double evalSol(S solution) {
            return this.evaluateSolution.applyAsDouble(solution);
        }

        @Override
        public double evalMove(M move) {
            return this.evaluateMove.applyAsDouble(move);
        }

        @Override
        public FMode getFMode() {
            return fMode;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}

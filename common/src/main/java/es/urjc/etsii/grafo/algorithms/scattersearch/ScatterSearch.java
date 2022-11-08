package es.urjc.etsii.grafo.algorithms.scattersearch;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.TimeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiPredicate;

public class ScatterSearch<S extends Solution<S, I>, I extends Instance> extends Algorithm<S, I> {

    private static final Logger log = LoggerFactory.getLogger(ScatterSearch.class);

    /**
     * During refset initialization, initialRefset * INITIAL_RATIO solutions,
     * to ensure we have enough non repeated and diverse solutions
     */
    private static final int INITIAL_RATIO = 5;

    private final int refsetSize;
    private final Constructive<S, I> constructive;
    private final Improver<S, I> improver;
    private final SolutionCombinator<S, I> combinator;
    private final int maxIterations;
    private final Comparator<S> bestValueSort;
    private final double ratio;
    private final BiPredicate<Double, Double> isBetterFunction;
    private final SolutionDistance<S, I> solutionDistance;
    private final boolean softRestartEnabled;

    /**
     * @param refsetSize       Number of solutions to keep in refset
     * @param constructive     Method used to generate the initial refset
     * @param improver         Method to improve any given solution, such as a local search
     * @param combinator       Creates a solution as a combination of two different solutions
     * @param diversityRatio   Porcentage of diverse solution to use relaive to the refset size.
     *                         0 means use only best value criteria, 1 use only diversity criteria,
     *                         0.5 half the refset uses diversity criteria, the other half best value criteria.
     * @param solutionDistance How to calculate distance between a given set of solutions
     */
    @AutoconfigConstructor
    public ScatterSearch(
            @IntegerParam(min = 10, max = 30) int refsetSize,
            Constructive<S, I> constructive,
            Improver<S, I> improver,
            SolutionCombinator<S, I> combinator,
            @ProvidedParam(type = ProvidedParamType.MAXIMIZE) boolean maximizing,
            @IntegerParam(min = 1) int maxIterations,
            @RealParam(min = 0, max = 1) double diversityRatio,
            SolutionDistance<S, I> solutionDistance
    ) {
        this.refsetSize = refsetSize;
        this.constructive = constructive;
        this.improver = improver;
        this.combinator = combinator;
        this.isBetterFunction = DoubleComparator.isBetterFunction(maximizing);
        this.softRestartEnabled = true;
        this.maxIterations = maxIterations;
        this.ratio = diversityRatio;
        this.solutionDistance = solutionDistance;

        Comparator<S> compareByScore = Comparator.comparing(Solution::getScore);
        // Comparator orders from less to more by default, if maximizing reverse ordering
        this.bestValueSort = maximizing ? compareByScore.reversed() : compareByScore;
    }

    protected RefSet<S, I> initializeRefset(I instance) {

        int solutionsByDiversity = (int) (refsetSize * ratio);
        int solutionsByScore = refsetSize - solutionsByDiversity;

        var initialSolutions = initializeRefSet(instance, refsetSize * INITIAL_RATIO);
        initialSolutions.sort(bestValueSort);

        // Copy pointers to best solutions only
        S[] initialRefsetArray = (S[]) new Object[refsetSize];
        for (int i = 0; i < solutionsByScore; i++) {
            initialRefsetArray[i] = initialSolutions.get(i);
        }

        var refset = addDiverseSolutions(initialRefsetArray, initialSolutions, solutionsByScore, solutionsByDiversity);

        return refset;
    }

    private RefSet<S,I> addDiverseSolutions(S[] initialRefsetArray, List<S> initialSolutions, int solutionsByScore, int solutionsByDiversity) {
        double[] minDistances = new double[initialSolutions.size()];
        boolean[] alreadyUsed = new boolean[initialSolutions.size()];

        // Calculate initial minimum distances for all candidate solutions
        for (int i = solutionsByScore; i < initialSolutions.size(); i++) {
            minDistances[i] = minDistanceToSolList(initialSolutions.get(i), initialRefsetArray);
        }

        // Add solutionsByDiversity number of solutions
        for (int i = 0; i < solutionsByDiversity; i++) {
            // Find index with minimum value that is not already used
            double max = Double.MIN_VALUE;
            int bestIndex = -1;
            for (int j = solutionsByScore; j < initialSolutions.size(); j++) {
                if(alreadyUsed[j]) continue;
                if(minDistances[j] > max){
                    max = minDistances[j];
                    bestIndex = j;
                }
            }
            // Add chosen solution
            assert max != Double.MIN_VALUE && bestIndex != -1;
            var chosenSolution = initialSolutions.get(bestIndex);
            alreadyUsed[bestIndex] = true;
            initialRefsetArray[solutionsByScore + i] = chosenSolution;

            // Fast update all minimum values, minimum distance can only decrease with currently added solution
            for (int j = solutionsByScore; j < initialSolutions.size(); j++) {
                if(alreadyUsed[j]) continue;
                double distanceToNewSolution = this.solutionDistance.distances(initialSolutions.get(j), chosenSolution);
                minDistances[j] = Math.min(minDistances[j], distanceToNewSolution);
            }
        }
        Arrays.sort(initialRefsetArray, this.bestValueSort);
        return new RefSet<>(initialRefsetArray, solutionsByScore, solutionsByDiversity);
    }

//    protected void recalculateDiversityDistances(RefSet<S, I> refSet) {
//        var byDiversity = new ArrayList<Pair<S, I>>(refSet.diversitySize);
//        for (var p : refSet.diversity) {
//            double value = avgDistanceToPairList(p.solution, refSet.diversity);
//            byDiversity.add(new Pair<>(value, p.solution));
//        }
//        refSet.diversity = byDiversity;
//    }

    protected RefSet<S, I> softRestart(I instance, RefSet<S, I> current) {
        var newRefset = initializeRefset(instance);
        // replace nearest worse solution with this one
        replaceWorstNearest(newRefset, current.solutions[0]);
        return newRefset;
    }

    /**
     * Try to insert the given solution in the refset. If the solution is better than all in refset it is always inserted.
     * if it is better than some, replace the nearest solution to given solution, and keep refset sorted
     * If the solution is worse than all in refset, the refset remains unchanged
     * @param refset Reference Set
     * @param solution Solution to try to insert in refset
     * @return true if refset is updated, false if refset remains unchanged
     */
    protected boolean replaceWorstNearest(RefSet<S, I> refset, S solution) {
        int i = 0;
        while (i < refset.solutions.length && this.isBetterFunction.test(refset.solutions[i].getScore(), solution.getScore())){
            // Might be speed up using binary search, but for small arrays it is not worth it
            i++;
        }
        if(i >= refset.solutions.length){
            // Best is the worst in new refset, strange but may happen, return
            return false;
        }

        // Find nearest element
        double min = Double.MAX_VALUE;
        int idx = -1;
        for(int j = i; j < refset.solutions.length; j++){
            double distance = this.solutionDistance.distances(solution, refset.solutions[j]);
            if(distance < min){
                idx = j;
                min = distance;
            }
        }

        // Move the nearest element to insertion index, it will be replaced by the new solution,
        // and array elements maintain correct order
        ArrayUtil.deleteAndInsert(refset.solutions, idx, i);
        refset.solutions[i] = solution;
        return true;
    }

    @Override
    public S algorithm(I instance) {
        var refset = initializeRefset(instance);

        int iterations = 0;
        while (iterations < maxIterations && !TimeControl.isTimeUp()) {
            var newSet = combinator.newSet(refset.solutions);
            var insertedElements = mergeToSetByScore(refset, newSet);
            debugStatus(refset, newSet, insertedElements);
            if (insertedElements.isEmpty()) {
                // If we have not found a single better value for the refset, do softrestart
                if (this.softRestartEnabled && !TimeControl.isTimeUp()) {
                    log.debug("Soft restart at iteration {} / {}", iterations, maxIterations);
                    refset = softRestart(instance, refset);
                } else {
                    log.debug("Ending at iteration {} / {}", iterations, maxIterations);
                    break;
                }
            }
            iterations++;
        }

        if (iterations == maxIterations) {
            log.debug("Ending, maxiter of {} reached.", maxIterations);
        }

        // Refset is always kept sorted
        return refset.solutions[0];
    }

    private void debugStatus(RefSet<S, I> refsets, List<S> newSet, List<S> mergeResultByValue) {
        if (!log.isDebugEnabled()) {
            return;
        }
        var sb = new StringBuilder();
        sb.append("[");
        for(var s: newSet){
            sb.append(s.getScore());
            sb.append(", ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        log.debug("Current: {}, new: {}, mergeByValue: {}", refsets, sb, mergeResultByValue);
    }

//    private double avgDistanceToSolList(S solution, List<S> initialDiverseSolutions) {
//        double sum = 0;
//        for (var s : initialDiverseSolutions) {
//            double v = this.solutionDistance.distances(solution, s);
//            sum += v;
//        }
//        return sum / initialDiverseSolutions.size();
//    }

//    private double avgDistanceToPairList(S solution, List<Pair<S, I>> initialDiverseSolutions) {
//        double sum = 0;
//        for (var s : initialDiverseSolutions) {
//            double v = this.solutionDistance.distances(solution, s.solution);
//            sum += v;
//        }
//        return sum / initialDiverseSolutions.size();
//    }

    private double minDistanceToSolList(S referenceSolution, S[] initialDiverseSolutions) {
        double min = Double.MAX_VALUE;
        for (var solution : initialDiverseSolutions) {
            if(solution == null) break;
            double v = this.solutionDistance.distances(referenceSolution, solution);
            min = Math.min(min, v);
        }
        assert min != Double.MAX_VALUE;
        return min;
    }


    protected List<S> initializeRefSet(I instance, int size) {
        List<S> initialSolutions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            var initializedSolution = this.newSolution(instance);
            var constructedSolution = this.constructive.construct(initializedSolution);
            var improvedSolution = this.improver.improve(constructedSolution);
            initialSolutions.add(improvedSolution);
        }
        return initialSolutions;
    }

    protected List<S> mergeToSetByScore(RefSet<S, I> refset, List<S> newSolutions) {
        double worstValue = refset.solutions[refset.solutions.length - 1].getScore();
        List<S> newElements = new ArrayList<>();

        newSolutions.sort(this.bestValueSort);

        for(var solution: newSolutions){
            if(refset.isInRefset(solution)){
                // Solution hash matched solution in current refset
                continue;
            }

            if(!(this.isBetterFunction.test(solution.getScore(), worstValue))){
                // Solution is worse than the worst solution in refset
                continue;
            }

            boolean inserted = replaceWorstNearest(refset, solution);
            if(inserted){
                newElements.add(solution);
            }
        }

        return newElements;
    }

    @Override
    public String toString() {
        return "ScatterS{" +
                "n=" + refsetSize +
                "r=" + ratio +
                ", const=" + constructive +
                ", impr=" + improver +
                ", comb=" + combinator +
                ", maxIter=" + maxIterations +
                '}';
    }

    protected record Pair<S extends Solution<S, I>, I extends Instance>(double score, S solution) {
        @Override
        public String toString() {
            return Double.toString(score);
        }
    }
}

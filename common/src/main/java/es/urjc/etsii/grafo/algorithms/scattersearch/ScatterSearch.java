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

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiPredicate;

public class ScatterSearch<S extends Solution<S, I>, I extends Instance> extends Algorithm<S, I> {

    private static final Logger log = LoggerFactory.getLogger(ScatterSearch.class);

    /**
     * If we build more than WARN_INIT_ITER_THRESHOLD and we still get duplicates, warn user that something wrong may be happening
     */
    private static int ERROR_INIT_ITER_THRESHOLD = 500;

    private final String name;
    /**
     * During refset initialization, initialRefset * INITIAL_RATIO solutions,
     * to ensure we have enough non repeated and diverse solutions
     */
    private final double initialRatio;
    private final int refsetSize;
    private final Constructive<S, I> constructiveGoodValues;
    private final Constructive<S, I> constructiveGoodDiversity;
    private final Improver<S, I> improver;
    private final SolutionCombinator<S, I> combinator;
    private final int maxIterations;
    private final Comparator<S> bestValueSort;
    private final double ratio;
    private final BiPredicate<Double, Double> isBetterFunction;
    private final SolutionDistance<S, I> solutionDistance;
    private final boolean softRestartEnabled;

    /**
     * @param initialRatio
     * @param refsetSize                Number of solutions to keep in refset
     * @param constructiveGoodValues    Method used to generate the initial refset
     * @param constructiveGoodDiversity
     * @param improver                  Method to improve any given solution, such as a local search
     * @param combinator                Creates a solution as a combination of two different solutions
     * @param diversityRatio            Porcentage of diverse solution to use relaive to the refset size.
     *                                  0 means use only best value criteria, 1 use only diversity criteria,
     *                                  0.5 half the refset uses diversity criteria, the other half best value criteria.
     * @param solutionDistance          How to calculate distance between a given set of solutions
     */
    @AutoconfigConstructor
    public ScatterSearch(
            @ProvidedParam(type = ProvidedParamType.ALGORITHM_NAME) String name,
            @RealParam(min = 1, max = 10) double initialRatio,
            @IntegerParam(min = 10, max = 30) int refsetSize,
            Constructive<S, I> constructiveGoodValues,
            Constructive<S, I> constructiveGoodDiversity,
            Improver<S, I> improver,
            SolutionCombinator<S, I> combinator,
            @ProvidedParam(type = ProvidedParamType.MAXIMIZE) boolean maximizing,
            @IntegerParam(min = 1) int maxIterations,
            @RealParam(min = 0, max = 1) double diversityRatio,
            SolutionDistance<S, I> solutionDistance
    ) {
        if(initialRatio < 1){
            throw new IllegalArgumentException("initialRatio must be > 0");
        }

        if(diversityRatio < 0 || diversityRatio > 1){
            throw new IllegalArgumentException("Diversity ratio must be in range [0, 1]");
        }

        this.name = name;
        this.initialRatio = initialRatio;
        this.refsetSize = refsetSize;
        this.constructiveGoodValues = constructiveGoodValues;
        this.constructiveGoodDiversity = constructiveGoodDiversity;
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

    protected RefSet<S, I> initializeRefset(Class<?> clazz, I instance) {
        int solutionsByDiversity = (int) (refsetSize * ratio);
        int solutionsByScore = refsetSize - solutionsByDiversity;

        var initialSolutions = initializeSolutions(instance, (int)(refsetSize * initialRatio), false);
        initialSolutions.sort(bestValueSort);

        // Copy pointers to best solutions only
        Set<S> alreadyUsed = new HashSet<>();
        S[] initialRefsetArray = (S[]) Array.newInstance(clazz, refsetSize);
        int assignedSolutionsByScore = 0;
        int i;
        for (i = 0; i < initialSolutions.size(); i++) {
            S initialSolution = initialSolutions.get(i);
            if (!alreadyUsed.contains(initialSolution)) {
                alreadyUsed.add(initialSolution);
                initialRefsetArray[assignedSolutionsByScore++] = initialSolution;
            }
            if (assignedSolutionsByScore == solutionsByScore) {
                break; // Completed refset fill by solution score
            }
        }

        // Force fill using diversity if there are too many duplicates
        if(assignedSolutionsByScore < solutionsByScore){
            log.debug("Failed to fill refsef using best values, currently {} assigned of {} with {} initial solutions, probably due to duplicates, more solutions are being automatically generated", assignedSolutionsByScore, refsetSize, initialSolutions.size());
            int forcedIter = 0;
            while(assignedSolutionsByScore < solutionsByScore){
                var initialSolution = this.initializeSolution(instance, true);
                if(!alreadyUsed.contains(initialSolution)){
                    alreadyUsed.add(initialSolution);
                    initialRefsetArray[assignedSolutionsByScore] = initialSolution;
                    assignedSolutionsByScore++;
                }
                forcedIter++;
                if(forcedIter == ERROR_INIT_ITER_THRESHOLD){
                    log.warn("TOO MANY ITERATIONS: Failed to fill refsef using best values, currently {} assigned of {} with {} initial solutions, debug what is happening", assignedSolutionsByScore, refsetSize, initialSolutions.size());
                }
            }
        }

        var filtered = new ArrayList<S>();
        for(var solution: initialSolutions){
            if(!alreadyUsed.contains(solution)){
                filtered.add(solution);
            }
        }

        int assignedSolutionsByDiversity = 0;

        double[] minDistances = new double[filtered.size()];
        boolean[] usedIndexes = new boolean[filtered.size()];

        // Calculate initial minimum distances for all candidate solutions
        for (int j = 0; j < filtered.size(); j++) {
            minDistances[j] = minDistanceToSolList(filtered.get(j), initialRefsetArray);
        }

        // Add solutionsByDiversity number of solutions
        for (int j = 0; j < solutionsByDiversity; j++) {
            // Find index with minimum value that is not already used
            double max = Integer.MIN_VALUE;
            int bestIndex = -1;
            for (int k = 0; k < filtered.size(); k++) {
                if(usedIndexes[k]) continue;
                if(minDistances[k] > max){
                    max = minDistances[k];
                    bestIndex = k;
                }
            }
            // Add chosen solution
            if(max == Integer.MIN_VALUE || bestIndex == -1){
                break; // No valid solution by diversity found, break and let the force fill repair the refset
            }
            var chosenSolution = filtered.get(bestIndex);
            usedIndexes[bestIndex] = true;
            initialRefsetArray[solutionsByScore + assignedSolutionsByDiversity] = chosenSolution;
            assignedSolutionsByDiversity++;

            // Fast update all minimum values, minimum distance can only decrease with currently added solution
            for (int k = 0; k < filtered.size(); k++) {
                if(usedIndexes[k]) continue;
                double distanceToNewSolution = this.solutionDistance.distances(filtered.get(k), chosenSolution);
                minDistances[k] = Math.min(minDistances[k], distanceToNewSolution);
            }
        }

        // Force fill if there are missing solutions not chosen by diversity
        if(assignedSolutionsByDiversity < solutionsByDiversity){
            log.debug("Failed to fill refsef by diversity, currently {} assigned of {} with {} initial solutions, probably due to duplicates, more solutions are being automatically generated", assignedSolutionsByDiversity, refsetSize, initialSolutions.size());
            int forcedIter = 0;
            while(assignedSolutionsByDiversity < solutionsByDiversity){
                var initialSolution = this.initializeSolution(instance, true);
                if(!alreadyUsed.contains(initialSolution)){
                    alreadyUsed.add(initialSolution);
                    initialRefsetArray[solutionsByScore + assignedSolutionsByDiversity] = initialSolution;
                    assignedSolutionsByDiversity++;
                }
                forcedIter++;
                if(forcedIter == ERROR_INIT_ITER_THRESHOLD){
                    log.warn("TOO MANY ITERATIONS: Failed to fill refsef using best values, currently {} assigned of {} with {} initial solutions, debug what is happening", assignedSolutionsByDiversity, refsetSize, initialSolutions.size());
                }
            }
        }


        Arrays.sort(initialRefsetArray, this.bestValueSort);
        return new RefSet<>(initialRefsetArray, solutionsByScore, solutionsByDiversity);
    }

    protected RefSet<S, I> softRestart(Class<?> clazz, I instance, RefSet<S, I> current) {
        var newRefset = initializeRefset(clazz, instance);
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
     */
    protected void replaceWorstNearest(RefSet<S, I> refset, S solution) {
        int i = 0;
        while (i < refset.solutions.length && !this.isBetterFunction.test(solution.getScore(), refset.solutions[i].getScore())){
            // Might be speed up using binary search, but for small arrays it is not worth it
            i++;
        }
        if(i >= refset.solutions.length){
            // Best is the worst in new refset, strange but may happen, return
            return;
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
        refset.currentRefset.remove(refset.solutions[i]);
        refset.currentRefset.add(solution);
        refset.solutions[i] = solution;
    }

    @Override
    public S algorithm(I instance) {
        // Obtain reference to real solution class implementation
        var clazz = getBuilder().initializeSolution(instance).getClass(); // a bit hacky, improve?

        var refset = initializeRefset(clazz, instance);
        Set<S> insertedSolutions = new HashSet<>(List.of(refset.solutions));
        debugStatus(0, refset, Set.of(), insertedSolutions);

        int iterations = 1;
        while (iterations <= maxIterations && !TimeControl.isTimeUp()) {
            var candidateSolutions = combinator.newSet(refset.solutions, insertedSolutions);
            insertedSolutions = mergeToSetByScore(refset, candidateSolutions);
            debugStatus(iterations, refset, candidateSolutions, insertedSolutions);
            if (insertedSolutions.isEmpty()) {
                // If we have not found a single better value for the refset, do softrestart
                if (this.softRestartEnabled && !TimeControl.isTimeUp()) {
                    log.debug("Soft restart at iteration {} / {}", iterations, maxIterations);
                    refset = softRestart(clazz, instance, refset);
                    insertedSolutions = new HashSet<>(refset.currentRefset);
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

    private void debugStatus(int iterations, RefSet<S, I> refsets, Set<S> newSet, Set<S> insertedSolutions) {
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug("{}. refset=[{},{}], |newSols|={}, |insertedSols|={}", iterations, refsets.solutions[0].getScore(), refsets.solutions[refsets.solutions.length-1].getScore(), newSet.size(), insertedSolutions.size());
        log.trace("{}. Current refset: {}, newSols: {}, mergeByValue: {}", iterations, refsets, newSet, insertedSolutions);
    }

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


    protected List<S> initializeSolutions(I instance, int size, boolean diverse) {
        List<S> initialSolutions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            var solution = initializeSolution(instance, diverse);
            initialSolutions.add(solution);
        }
        return initialSolutions;
    }

    protected S initializeSolution(I instance, boolean diverse){
        var solution = this.newSolution(instance);
        if(diverse){
            solution = this.constructiveGoodDiversity.construct(solution);
        } else {
            solution = this.constructiveGoodValues.construct(solution);
        }
        var improvedSolution = this.improver.improve(solution);
        return improvedSolution;
    }

    protected Set<S> mergeToSetByScore(RefSet<S, I> refset, Set<S> newSolutions) {
        double worstValue = refset.solutions[refset.solutions.length - 1].getScore();
        Set<S> insertedElements = new HashSet<>();

        var sortedNewSolutions = new ArrayList<>(newSolutions);
        sortedNewSolutions.sort(this.bestValueSort);

        for(var solution: sortedNewSolutions){
            if(!(this.isBetterFunction.test(solution.getScore(), worstValue))){
                // Solution is worse than the worst solution in refset
                break; // as newSolutions are ordered by value, if the current one is worse, all remaining must be worse too
            }

            if(refset.isInRefset(solution)){
                // Solution hash matched solution in current refset, ignore
                continue;
            }

            replaceWorstNearest(refset, solution);
            insertedElements.add(solution);
            worstValue = refset.solutions[refset.solutions.length - 1].getScore();
        }

        if(insertedElements.size() > refset.solutions.length){
            throw new IllegalStateException("Bug detected");
        }
        return insertedElements;
    }

    @Override
    public String toString() {
        return "ScatterS{" +
                "n=" + refsetSize +
                "r=" + ratio +
                ", comb=" + combinator +
                ", dist=" + solutionDistance +
                ", const=" + constructiveGoodValues +
                ", impr=" + improver +
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

package es.urjc.etsii.grafo.algorithms.scattersearch;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
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

import static java.util.Comparator.comparingDouble;

public class ScatterSearch<S extends Solution<S, I>, I extends Instance> extends Algorithm<S, I> {

    private static final Logger log = LoggerFactory.getLogger(ScatterSearch.class);

    /**
     * If we build more than WARN_INIT_ITER_THRESHOLD and we still get duplicates, warn user that something wrong may be happening
     */
    private static final int ERROR_INIT_ITER_THRESHOLD = 500;

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
    private final double ratio;
    private final SolutionDistance<S, I> solutionDistance;
    private final boolean softRestartEnabled;
    private final FMode fmode;

    /**
     * @param initialRatio              During refset initialization, create initialRefset size * INITIAL_RATIO solutions,
     *                                  to ensure we have enough non repeated and diverse solutions
     * @param refsetSize                Number of solutions to keep in refset
     * @param constructiveGoodValues    Method used to generate the initial refset
     * @param constructiveGoodDiversity Method used to generate diverse solutions
     * @param improver                  Method to improve any given solution, such as a local search
     * @param combinator                Creates a solution as a combination of two different solutions
     * @param diversityRatio            Porcentage of diverse solution to use relaive to the refset size.
     *                                  0 means use only best value criteria, 1 use only diversity criteria,
     *                                  0.5 half the refset uses diversity criteria, the other half best value criteria.
     * @param solutionDistance          How to calculate distance between a given set of solutions. See {@link SolutionDistance} for more details.
     */
    @AutoconfigConstructor
    public ScatterSearch(
            @ProvidedParam String name,
            @RealParam(min = 1, max = 10) double initialRatio,
            @IntegerParam(min = 10, max = 30) int refsetSize,
            Constructive<S, I> constructiveGoodValues,
            Constructive<S, I> constructiveGoodDiversity,
            Improver<S, I> improver,
            SolutionCombinator<S, I> combinator,
            @ProvidedParam FMode fmode,
            @IntegerParam(min = 1) int maxIterations,
            @RealParam(min = 0, max = 1) double diversityRatio,
            SolutionDistance<S, I> solutionDistance
    ) {
        super(name);

        this.initialRatio = initialRatio;
        this.refsetSize = refsetSize;
        this.constructiveGoodValues = Objects.requireNonNull(constructiveGoodValues);
        this.constructiveGoodDiversity = Objects.requireNonNull(constructiveGoodDiversity);
        this.improver = Objects.requireNonNull(improver);
        this.combinator = Objects.requireNonNull(combinator);
        this.fmode = fmode;
        this.softRestartEnabled = true;
        this.maxIterations = maxIterations;
        this.ratio = diversityRatio;
        this.solutionDistance = Objects.requireNonNull(solutionDistance);
    }

    protected RefSet<S, I> initializeRefset(Class<?> clazz, I instance) {
        int nSolutionsByDiversity = (int) (refsetSize * ratio);
        int nSolutionsByScore = refsetSize - nSolutionsByDiversity;

        Set<S> alreadyUsed = new HashSet<>();
        S[] initialRefsetArray = (S[]) Array.newInstance(clazz, refsetSize);

        var initialSolutions = initializeSolutions(instance, (int) (nSolutionsByScore * initialRatio), false);
        initialSolutions.addAll(initializeSolutions(instance, (int) (nSolutionsByDiversity * initialRatio), true));
        initialSolutions.sort(fmode.comparator());

        int assignedSolutionsByScore = 0;
        for (S initialSolution : initialSolutions) {
            if (!alreadyUsed.contains(initialSolution)) {
                alreadyUsed.add(initialSolution);
                initialRefsetArray[assignedSolutionsByScore++] = initialSolution;
            }
            if (assignedSolutionsByScore == nSolutionsByScore) {
                break; // Completed refset fill by solution score
            }
        }

        // Force fill using diversity if there are too many duplicates
        forceFill(instance, alreadyUsed, initialRefsetArray, 0, assignedSolutionsByScore, nSolutionsByScore, initialSolutions.size());

        if (nSolutionsByDiversity > 0) {
            record SolutionDistancePair<S extends Solution<S,I>, I extends Instance>(S solution, double distance){}

            var unusedSolutions = new ArrayList<SolutionDistancePair<S, I>>();
            for (var solution : initialSolutions) {
                if (!alreadyUsed.contains(solution)) {
                    double distance = minDistanceToSolList(solution, initialRefsetArray);
                    unusedSolutions.add(new SolutionDistancePair<>(solution, distance));
                }
            }

            int assignedSolutionsByDiversity = 0;
            // Add solutionsByDiversity number of solutions

            for (int i = 0; i < nSolutionsByDiversity; i++) {
                if(unusedSolutions.isEmpty()) break;
                unusedSolutions.sort(comparingDouble(SolutionDistancePair::distance));

                // Most diverse solution is last in the array, as it will have the maximum min distance
                var chosenPair = unusedSolutions.remove(unusedSolutions.size() - 1);

                initialRefsetArray[nSolutionsByScore + assignedSolutionsByDiversity] = chosenPair.solution;
                assignedSolutionsByDiversity++;

                // Fast update all minimum distance values, minimum distance can only decrease with currently added solution
                var newUnused = new ArrayList<SolutionDistancePair<S, I>>();
                for(var pair: unusedSolutions){
                    double distanceToNewSolution = this.solutionDistance.distances(pair.solution, chosenPair.solution);
                    double newMinimumDistance = Math.min(distanceToNewSolution, pair.distance);
                    newUnused.add(new SolutionDistancePair<>(pair.solution, newMinimumDistance));
                }
                unusedSolutions = newUnused;
            }

            // Force fill if there are missing solutions not chosen by diversity
            forceFill(instance, alreadyUsed, initialRefsetArray, nSolutionsByScore, assignedSolutionsByDiversity, nSolutionsByDiversity, initialSolutions.size());
        }

        Arrays.sort(initialRefsetArray, fmode.comparator());
        return new RefSet<>(initialRefsetArray, nSolutionsByScore, nSolutionsByDiversity);
    }

    protected void forceFill(I instance, Set<S> alreadyUsed, S[] initialRefsetArray, int offset, int assignedSolutions, int nRequiredSolutions, int nInitialSolutions) {
        if (assignedSolutions < nRequiredSolutions) {
            log.debug("Failed to fill refsef {}, currently {} assigned of {} with {} initial solutions, probably due to duplicates, more solutions are being automatically generated",
                    offset == 0 ? "by best value" : "by diversity", assignedSolutions, refsetSize, nInitialSolutions);
            int forcedIter = 0;
            while (assignedSolutions < nRequiredSolutions) {
                var initialSolution = this.initializeSolution(instance, true);
                if (!alreadyUsed.contains(initialSolution)) {
                    alreadyUsed.add(initialSolution);
                    initialRefsetArray[offset + assignedSolutions] = initialSolution;
                    assignedSolutions++;
                }
                forcedIter++;
                if (forcedIter == ERROR_INIT_ITER_THRESHOLD) {
                    log.warn("TOO MANY ITERATIONS: Failed to fill refsef {}, currently {} assigned of {} with {} initial solutions, debug what is happening",
                            offset == 0 ? "by best value" : "by diversity", assignedSolutions, refsetSize, nInitialSolutions);
                }
            }
        }
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
     *
     * @param refset   Reference Set
     * @param solution Solution to try to insert in refset
     */
    protected void replaceWorstNearest(RefSet<S, I> refset, S solution) {
        int i = 0;
        while (i < refset.solutions.length && !fmode.isBetter(solution.getScore(), refset.solutions[i].getScore())) {
            // Might be speed up using binary search, but for small arrays it is not worth it
            i++;
        }
        if (i >= refset.solutions.length) {
            // Best is the worst in new refset, strange but may happen, return
            return;
        }

        // Find nearest element
        double min = Double.MAX_VALUE;
        int idx = -1;
        for (int j = i; j < refset.solutions.length; j++) {
            double distance = this.solutionDistance.distances(solution, refset.solutions[j]);
            if (distance < min) {
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

    protected void debugStatus(int iterations, RefSet<S, I> refsets, Set<S> newSet, Set<S> insertedSolutions) {
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug("{}. refset=[{},{}], |newSols|={}, |insertedSols|={}", iterations, refsets.solutions[0].getScore(), refsets.solutions[refsets.solutions.length - 1].getScore(), newSet.size(), insertedSolutions.size());
        log.trace("{}. Current refset: {}, newSols: {}, mergeByValue: {}", iterations, refsets, newSet, insertedSolutions);
    }

    protected double minDistanceToSolList(S referenceSolution, S[] initialDiverseSolutions) {
        double min = Double.MAX_VALUE;
        for (var solution : initialDiverseSolutions) {
            if (solution == null) break;
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

    protected S initializeSolution(I instance, boolean diverse) {
        var solution = this.newSolution(instance);
        if (diverse) {
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
        sortedNewSolutions.sort(fmode.comparator());

        for (var solution : sortedNewSolutions) {
            if (!(fmode.isBetter(solution.getScore(), worstValue))) {
                // Solution is worse than the worst solution in refset
                break; // as newSolutions are ordered by value, if the current one is worse, all remaining must be worse too
            }

            if (refset.isInRefset(solution)) {
                // Solution hash matched solution in current refset, ignore
                continue;
            }

            replaceWorstNearest(refset, solution);
            insertedElements.add(solution);
            worstValue = refset.solutions[refset.solutions.length - 1].getScore();
        }

        if (insertedElements.size() > refset.solutions.length) {
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
}

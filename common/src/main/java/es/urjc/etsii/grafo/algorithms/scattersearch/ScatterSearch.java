package es.urjc.etsii.grafo.algorithms.scattersearch;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.TimeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;

public class ScatterSearch<S extends Solution<S, I>, I extends Instance> extends Algorithm<S, I> {

    private static final Logger log = LoggerFactory.getLogger(ScatterSearch.class);

    private final int refsetSize;
    private final Constructive<S, I> constructive;
    private final Improver<S, I> improver;
    private final SolutionCombinator<S, I> combinator;
    private final int maxIterations;
    private final Comparator<Pair<S, I>> bestValueSort;
    private final Comparator<Pair<S, I>> diversitySort;
    private final double ratio;
    private final BiPredicate<Double, Double> isBetterFunction;
    private final SolutionDistance<S, I> solutionDistance;
    private final BiPredicate<Double, Double> isMoreDiverseFunction;
    private final boolean softRestart;

    /**
     * @param refsetSize       Number of solutions to keep in refset
     * @param constructive     Method used to generate the initial refset
     * @param improver         Method to improve any given solution, such as a local search
     * @param combinator       Creates a solution as a combination of two different solutions
     * @param diversityRatio   Porcentage of diverse solution to use relaive to the refset size.
     *                         0 means use only best value criteria, 1 use only diversity criteria,
     *                         0.5 half the refset uses diversity criteria, the other half best value criteria.
     * @param solutionDistance How to calculate distance between a given set of solutions
     * @param softRestart      if the refset converges but we have more iterations or time available,
     *                         delete all solutions except the most diverse and the best and recreate refset
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
            //, boolean softRestart
    ) {
        this.refsetSize = refsetSize;
        this.constructive = constructive;
        this.improver = improver;
        this.combinator = combinator;
        this.isBetterFunction = DoubleComparator.isBetterFunction(maximizing);
        this.softRestart = true;
        this.isMoreDiverseFunction = DoubleComparator::isGreater;
        this.maxIterations = maxIterations;
        this.ratio = diversityRatio;
        this.solutionDistance = solutionDistance;

        Comparator<Pair<S, I>> compareByScore = Comparator.comparing(Pair::score);
        // Comparator orders from less to more by default, if maximizing reverse ordering
        this.bestValueSort = maximizing ? compareByScore.reversed() : compareByScore;
        this.diversitySort = compareByScore.reversed();
    }

    protected RefSets<S, I> initializeRefsets(I instance) {
        int diversityRefSetSize = (int) (refsetSize * ratio);
        var diversityRefSet = new ArrayList<Pair<S, I>>(diversityRefSetSize);

        var initialDiverseSolutions = initializeRefSet(instance, diversityRefSetSize);
        for (var s : initialDiverseSolutions) {
            double value = this.avgDistanceToSolList(s, initialDiverseSolutions);
            diversityRefSet.add(new Pair<>(value, s));
        }
        diversityRefSet.sort(this.diversitySort);

        int bestRefSetSize = refsetSize - diversityRefSetSize;
        var bestRefSet = new ArrayList<Pair<S, I>>(bestRefSetSize);
        var initialBestSolutions = initializeRefSet(instance, bestRefSetSize);
        for (var s : initialBestSolutions) {
            bestRefSet.add(new Pair<>(s.getScore(), s));
        }
        bestRefSet.sort(this.bestValueSort);

        return new RefSets<>(diversityRefSet, diversityRefSetSize, bestRefSet, bestRefSetSize);
    }

    protected void recalculateDiversityDistances(RefSets<S,I> refSets){
        var byDiversity = new ArrayList<Pair<S, I>>(refSets.diversitySize);
        for(var p: refSets.diversity){
            double value = avgDistanceToPairList(p.solution, refSets.diversity);
            byDiversity.add(new Pair<>(value, p.solution));
        }
        refSets.diversity = byDiversity;
    }

    protected RefSets<S, I> softRestart(I instance, RefSets<S, I> current) {
        var newRefset = initializeRefsets(instance);
        if(!current.best.isEmpty()){
            var currBest = current.best.get(0);
            newRefset.best.add(currBest);
            newRefset.best.sort(bestValueSort);
            newRefset.best.remove(newRefset.best.size()-1);
        }
        if(!current.diversity.isEmpty()){
            var currBest = current.diversity.get(0);
            newRefset.diversity.add(currBest);
            newRefset.diversity.sort(diversitySort);
            newRefset.diversity.remove(newRefset.diversity.size()-1);
            recalculateDiversityDistances(newRefset);
        }
        // Verify that refset sizes are constant
        assert newRefset.best.size() == current.best.size();
        assert newRefset.diversity.size() == current.diversity.size();

        return newRefset;
    }

    @Override
    public S algorithm(I instance) {
        var refsets = initializeRefsets(instance);

        int iterations = 0;
        while (iterations < maxIterations && !TimeControl.isTimeUp()) {
            var newSet = combinator.newSet(merge(refsets.best, refsets.diversity));
            var mergeResultByValue = mergeSetsByValue(refsets.best, refsets.bestSize, newSet);
            var mergeResultByDiversity = mergeSetsByDiversity(refsets.diversity, refsets.diversitySize, newSet);

            if (!mergeResultByValue.updated() && !mergeResultByDiversity.updated) {
                if(this.softRestart && !TimeControl.isTimeUp()){
                    log.debug("Soft restart at iteration {} / {}", iterations, maxIterations);
                    refsets = softRestart(instance, refsets);
                } else {
                    log.debug("Ending at iteration {} / {}", iterations, maxIterations);
                    break;
                }
            }
            refsets.best = mergeResultByValue.refset;
            if(mergeResultByDiversity.updated){
                refsets.diversity = mergeResultByDiversity.refset;
                recalculateDiversityDistances(refsets);
            }
            iterations++;
        }

        if (iterations == maxIterations) {
            log.debug("Ending, maxiter of {} reached.", maxIterations);
        }

        return refsets.best.get(0).solution;
    }


    private double avgDistanceToSolList(S solution, List<S> initialDiverseSolutions) {
        double sum = 0;
        for (var s : initialDiverseSolutions) {
            double v = this.solutionDistance.distances(solution, s);
            sum += v;
        }
        return sum / initialDiverseSolutions.size();
    }

    private double avgDistanceToPairList(S solution, List<Pair<S, I>> initialDiverseSolutions) {
        double sum = 0;
        for (var s : initialDiverseSolutions) {
            double v = this.solutionDistance.distances(solution, s.solution);
            sum += v;
        }
        return sum / initialDiverseSolutions.size();
    }

    private List<S> merge(ArrayList<Pair<S, I>> bestValueRefSet, ArrayList<Pair<S, I>> diversityRefSet) {
        var list = new ArrayList<S>(bestValueRefSet.size() + diversityRefSet.size());
        for (var p : bestValueRefSet) {
            list.add(p.solution);
        }
        for (var p : diversityRefSet) {
            list.add(p.solution);
        }
        return list;
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



    protected MergeResult<S, I> mergeSetsByValue(List<Pair<S, I>> refset, int bestRefSetSize, List<S> newSet) {
        var newSetPairs = new ArrayList<Pair<S, I>>(newSet.size());
        for (var s : newSet) {
            double value = s.getScore();
            newSetPairs.add(new Pair<>(value, s));
        }

        newSetPairs.sort(this.bestValueSort);
        int leftIndex = 0, rightIndex = 0;
        var result = new ArrayList<Pair<S, I>>();
        boolean modified = false;
        assert refset.size() == bestRefSetSize;

        for (int i = 0; i < bestRefSetSize; i++) {
            // The first two ifs should NEVER execute, leave them ready in case we allow
            // refset resizing in the future
            if (leftIndex >= refset.size()) {
                throw new AssertionError("Refset emptied");
            } else if (rightIndex >= newSetPairs.size()) {
                throw new AssertionError("Newset emptied");
            } else {
                var left = refset.get(leftIndex);
                var right = newSetPairs.get(rightIndex);
                if (this.isBetterFunction.test(right.score, left.score)) {
                    result.add(right);
                    rightIndex++;
                    modified = true;
                } else {
                    result.add(left);
                    leftIndex++;
                }
            }
        }
        return new MergeResult<>(modified, result);
    }

    protected MergeResult<S, I> mergeSetsByDiversity(List<Pair<S, I>> refset, int diversityRefSetSize, List<S> newSet) {
        var newSetPairs = new ArrayList<Pair<S, I>>(newSet.size());
        for (var s : newSet) {
            double value = avgDistanceToPairList(s, refset);
            newSetPairs.add(new Pair<>(value, s));
        }
        newSetPairs.sort(this.diversitySort);

        int leftIndex = 0, rightIndex = 0;
        var result = new ArrayList<Pair<S, I>>();
        boolean modified = false;
        assert refset.size() == diversityRefSetSize;

        for (int i = 0; i < diversityRefSetSize; i++) {
            // The first two ifs should NEVER execute, leave them ready in case we allow
            // refset resizing in the future
            if (leftIndex >= refset.size()) {
                throw new AssertionError("Refset emptied");
            } else if (rightIndex >= newSetPairs.size()) {
                throw new AssertionError("Newset emptied");
            } else {
                var left = refset.get(leftIndex);
                var right = newSetPairs.get(rightIndex);
                if (isMoreDiverseFunction.test(right.score, left.score)) {
                    result.add(right);
                    rightIndex++;
                    modified = true;
                } else {
                    result.add(left);
                    leftIndex++;
                }
            }
        }
        return new MergeResult<>(modified, result);
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

    protected record MergeResult<S extends Solution<S, I>, I extends Instance>(boolean updated,
                                                                               ArrayList<Pair<S, I>> refset) {
    }

    protected record Pair<S extends Solution<S, I>, I extends Instance>(double score, S solution) {
    }
}

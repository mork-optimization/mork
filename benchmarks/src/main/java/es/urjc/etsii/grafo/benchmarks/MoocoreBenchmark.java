// SPDX-License-Identifier: MIT
package es.urjc.etsii.grafo.benchmarks;

import es.urjc.etsii.grafo.moocore.HypeDistribution;
import es.urjc.etsii.grafo.moocore.HypervolumeApproximation;
import es.urjc.etsii.grafo.moocore.MooCore;
import es.urjc.etsii.grafo.moocore.NondominatedSetShape;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class MoocoreBenchmark {

    @Benchmark
    public boolean[] nondominated(ParetoState state) {
        return MooCore.isNondominated(state.points);
    }

    @Benchmark
    public int[] paretoRank(ParetoState state) {
        return MooCore.paretoRank(state.points);
    }

    @Benchmark
    public boolean[] nondominatedStress(ParetoStressState state) {
        return MooCore.isNondominated(state.points);
    }

    @Benchmark
    public boolean anyDominatedStress(ParetoStressState state) {
        return MooCore.anyDominated(state.points);
    }

    @Benchmark
    public double[][] filterDominatedStress(ParetoStressState state) {
        return MooCore.filterDominated(state.points);
    }

    @Benchmark
    public int[] paretoRankStress(ParetoStressState state) {
        return MooCore.paretoRank(state.points);
    }

    @Benchmark
    public int[] paretoRankChain(ParetoRankChainState state) {
        return MooCore.paretoRank(state.points);
    }

    @Benchmark
    public double igd(IndicatorState state) {
        return MooCore.igd(state.points, state.reference);
    }

    @Benchmark
    public double igdPlus(IndicatorState state) {
        return MooCore.igdPlus(state.points, state.reference);
    }

    @Benchmark
    public double averageHausdorffDistance(IndicatorState state) {
        return MooCore.averageHausdorffDistance(state.points, state.reference, state.p);
    }

    @Benchmark
    public double epsilonAdditive(IndicatorState state) {
        return MooCore.epsilonAdditive(state.points, state.reference);
    }

    @Benchmark
    public double epsilonMultiplicative(IndicatorState state) {
        return MooCore.epsilonMultiplicative(state.positivePoints, state.positiveReference);
    }

    @Benchmark
    public double exactR2(ExactR2State state) {
        return MooCore.exactR2(state.points, state.reference);
    }

    @Benchmark
    public double hypervolume(HypervolumeState state) {
        return MooCore.hypervolume(state.points, state.reference);
    }

    @Benchmark
    public double[] hypervolumeContributions(HypervolumeContributionState state) {
        return MooCore.hypervolumeContributions(state.points, state.reference);
    }

    @Benchmark
    public double[][] eaf(EafState state) {
        return MooCore.eaf(state.points, state.sets);
    }

    @Benchmark
    public double weightedHypervolume(WeightedHypervolumeState state) {
        return MooCore.weightedHypervolume(state.points, state.rectangles, state.reference);
    }

    @Benchmark
    public double hypeWeightedHypervolume(HypeState state) {
        return MooCore.hypeWeightedHypervolume(state.points, state.reference, state.ideal,
                new boolean[]{false}, state.samples, 47L, state.distribution, state.mu);
    }

    @Benchmark
    public double approximateHypervolumeMonteCarlo(HypervolumeApproximationState state) {
        return MooCore.approximateHypervolume(state.points, state.reference,
                new boolean[]{false}, state.samples, 59L, HypervolumeApproximation.DZ2019_MC);
    }

    @Benchmark
    public double[][] generateNondominatedSet(NondominatedSetGenerationState state) {
        return MooCore.generateNondominatedSet(
                state.size, state.objectives, state.shape, 67L);
    }

    @State(Scope.Benchmark)
    public static class ParetoState {

        @Param({"100", "1000", "10000"})
        int size;

        @Param({"2", "3", "5"})
        int objectives;

        double[][] points;

        @Setup(Level.Trial)
        public void setup() {
            points = randomPoints(size, objectives, 17L);
        }
    }

    @State(Scope.Benchmark)
    public static class ParetoStressState {

        @Param({"500", "1000", "2000", "4000"})
        int size;

        @Param({"4", "5", "9"})
        int objectives;

        @Param({"RANDOM", "SIMPLEX"})
        ParetoShape shape;

        double[][] points;

        @Setup(Level.Trial)
        public void setup() {
            points = switch (shape) {
                case RANDOM -> randomPoints(size, objectives, 53L);
                case SIMPLEX -> simplexPoints(size, objectives, 53L);
            };
        }
    }

    @State(Scope.Benchmark)
    public static class ParetoRankChainState {

        @Param({"100", "250", "500"})
        int size;

        @Param({"4", "5", "9"})
        int objectives;

        double[][] points;

        @Setup(Level.Trial)
        public void setup() {
            points = chainPoints(size, objectives);
        }
    }

    @State(Scope.Benchmark)
    public static class IndicatorState {

        @Param({"100", "500"})
        int size;

        @Param({"2", "5", "9"})
        int objectives;

        @Param("2")
        int p;

        double[][] points;
        double[][] reference;
        double[][] positivePoints;
        double[][] positiveReference;

        @Setup(Level.Trial)
        public void setup() {
            points = randomPoints(size, objectives, 71L);
            reference = randomPoints(size, objectives, 73L);
            positivePoints = shifted(points, 0.5);
            positiveReference = shifted(reference, 0.5);
        }
    }

    @State(Scope.Benchmark)
    public static class ExactR2State {

        @Param({"100", "1000", "10000"})
        int size;

        double[][] points;
        double[] reference;

        @Setup(Level.Trial)
        public void setup() {
            points = randomPoints(size, 2, 79L);
            reference = new double[]{0.0, 0.0};
        }
    }

    @State(Scope.Benchmark)
    public static class HypervolumeState {

        @Param({"100", "500"})
        int size;

        @Param({"2", "3", "4"})
        int objectives;

        @Param("RANDOM")
        ParetoShape shape;

        double[][] points;
        double[] reference;

        @Setup(Level.Trial)
        public void setup() {
            points = switch (shape) {
                case RANDOM -> randomPoints(size, objectives, 29L);
                case SIMPLEX -> simplexPoints(size, objectives, 29L);
            };
            reference = new double[objectives];
            for (int objective = 0; objective < objectives; objective++) {
                reference[objective] = 1.0;
            }
        }
    }

    @State(Scope.Benchmark)
    public static class HypervolumeContributionState {

        @Param({"100", "500"})
        int size;

        @Param({"2", "3"})
        int objectives;

        @Param({"RANDOM", "SIMPLEX"})
        ParetoShape shape;

        double[][] points;
        double[] reference;

        @Setup(Level.Trial)
        public void setup() {
            points = switch (shape) {
                case RANDOM -> randomPoints(size, objectives, 31L);
                case SIMPLEX -> simplexPoints(size, objectives, 31L);
            };
            reference = new double[objectives];
            for (int objective = 0; objective < objectives; objective++) {
                reference[objective] = 1.0;
            }
        }
    }

    @State(Scope.Benchmark)
    public static class EafState {

        @Param({"100", "1000"})
        int size;

        @Param({"2", "3"})
        int objectives;

        @Param({"RANDOM", "SIMPLEX"})
        ParetoShape shape;

        double[][] points;
        int[] sets;

        @Setup(Level.Trial)
        public void setup() {
            points = switch (shape) {
                case RANDOM -> randomPoints(size, objectives, 41L);
                case SIMPLEX -> simplexPoints(size, objectives, 41L);
            };
            sets = new int[size];
            for (int row = 0; row < size; row++) {
                sets[row] = row % 10;
            }
        }
    }

    @State(Scope.Benchmark)
    public static class WeightedHypervolumeState {

        @Param({"100", "1000"})
        int size;

        @Param({"10", "100"})
        int rectangleCount;

        @Param({"RANDOM", "SIMPLEX"})
        ParetoShape shape;

        double[][] points;
        double[][] rectangles;
        double[] reference;

        @Setup(Level.Trial)
        public void setup() {
            points = switch (shape) {
                case RANDOM -> randomPoints(size, 2, 43L);
                case SIMPLEX -> simplexPoints(size, 2, 43L);
            };
            rectangles = weightedRectangles(rectangleCount);
            reference = new double[]{1.0, 1.0};
        }
    }

    @State(Scope.Benchmark)
    public static class HypeState {

        @Param("100")
        int size;

        @Param("10000")
        int samples;

        @Param({"UNIFORM", "EXPONENTIAL", "POINT"})
        HypeDistribution distribution;

        double[][] points;
        double[] reference;
        double[] ideal;
        double[] mu;

        @Setup(Level.Trial)
        public void setup() {
            points = randomPoints(size, 2, 47L);
            reference = new double[]{1.0, 1.0};
            ideal = new double[]{0.0, 0.0};
            mu = switch (distribution) {
                case UNIFORM -> null;
                case EXPONENTIAL -> new double[]{0.2};
                case POINT -> new double[]{0.5, 0.5};
            };
        }
    }

    @State(Scope.Benchmark)
    public static class HypervolumeApproximationState {

        @Param("100")
        int size;

        @Param({"2", "5", "9"})
        int objectives;

        @Param("10000")
        int samples;

        double[][] points;
        double[] reference;

        @Setup(Level.Trial)
        public void setup() {
            points = randomPoints(size, objectives, 59L);
            reference = new double[objectives];
            for (int objective = 0; objective < objectives; objective++) {
                reference[objective] = 1.0;
            }
        }
    }

    @State(Scope.Benchmark)
    public static class NondominatedSetGenerationState {

        @Param({"100", "500"})
        int size;

        @Param({"2", "5"})
        int objectives;

        @Param({"SIMPLEX", "CONCAVE_SPHERE"})
        NondominatedSetShape shape;
    }

    private static double[][] randomPoints(int size, int objectives, long seed) {
        Random random = new Random(seed);
        double[][] points = new double[size][objectives];
        for (int row = 0; row < size; row++) {
            for (int objective = 0; objective < objectives; objective++) {
                points[row][objective] = random.nextDouble();
            }
        }
        return points;
    }

    private static double[][] simplexPoints(int size, int objectives, long seed) {
        Random random = new Random(seed);
        double[][] points = new double[size][objectives];
        for (int row = 0; row < size; row++) {
            double total = 0.0;
            for (int objective = 0; objective < objectives; objective++) {
                double value = -Math.log(Math.max(random.nextDouble(), Double.MIN_NORMAL));
                points[row][objective] = value;
                total += value;
            }
            for (int objective = 0; objective < objectives; objective++) {
                points[row][objective] /= total;
            }
        }
        return points;
    }

    private static double[][] weightedRectangles(int count) {
        double[][] rectangles = new double[count][5];
        for (int i = 0; i < count; i++) {
            rectangles[i][0] = i / (double) count;
            rectangles[i][1] = 0.0;
            rectangles[i][2] = (i + 1.0) / count;
            rectangles[i][3] = 1.0;
            rectangles[i][4] = 0.5 + (i % 7) / 7.0;
        }
        return rectangles;
    }

    private static double[][] shifted(double[][] input, double offset) {
        double[][] result = new double[input.length][input[0].length];
        for (int row = 0; row < input.length; row++) {
            for (int objective = 0; objective < input[row].length; objective++) {
                result[row][objective] = input[row][objective] + offset;
            }
        }
        return result;
    }

    private static double[][] chainPoints(int size, int objectives) {
        double[][] points = new double[size][objectives];
        for (int row = 0; row < size; row++) {
            double value = row;
            for (int objective = 0; objective < objectives; objective++) {
                points[row][objective] = value;
            }
        }
        return points;
    }

    public enum ParetoShape {
        RANDOM,
        SIMPLEX
    }
}

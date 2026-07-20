// SPDX-License-Identifier: MIT
package es.urjc.etsii.grafo.benchmarks;

import es.urjc.etsii.grafo.moocore.MooCore;
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
    public double hypervolume(HypervolumeState state) {
        return MooCore.hypervolume(state.points, state.reference);
    }

    @Benchmark
    public double[][] eaf(EafState state) {
        return MooCore.eaf(state.points, state.sets);
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
    public static class HypervolumeState {

        @Param({"100", "500"})
        int size;

        @Param({"2", "3", "4"})
        int objectives;

        double[][] points;
        double[] reference;

        @Setup(Level.Trial)
        public void setup() {
            points = randomPoints(size, objectives, 29L);
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

        double[][] points;
        int[] sets;

        @Setup(Level.Trial)
        public void setup() {
            points = randomPoints(size, objectives, 41L);
            sets = new int[size];
            for (int row = 0; row < size; row++) {
                sets[row] = row % 10;
            }
        }
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
}

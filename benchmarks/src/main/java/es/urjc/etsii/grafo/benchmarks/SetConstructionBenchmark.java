package es.urjc.etsii.grafo.benchmarks;

import es.urjc.etsii.grafo.util.collections.BitSet;
import org.openjdk.jmh.annotations.*;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(3)
public class SetConstructionBenchmark {

    @Benchmark
    public HashSet<Integer> hashSet(BenchmarkData data) {
        return data.newHashSet(data.presentElements);
    }

    @Benchmark
    public BitSet bitSet(BenchmarkData data) {
        return data.newBitSet(data.presentElements);
    }
}

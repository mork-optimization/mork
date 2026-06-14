package es.urjc.etsii.grafo.benchmarks;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(3)
public class SetIterationBenchmark {

    @Benchmark
    public long hashSet(BenchmarkData data) {
        long sum = 0;
        for (int element : data.hashSet) {
            sum += element;
        }
        return sum;
    }

    @Benchmark
    public long bitSet(BenchmarkData data) {
        long sum = 0;
        for (int element : data.bitSet) {
            sum += element;
        }
        return sum;
    }
}

package es.urjc.etsii.grafo.benchmarks;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(3)
public class SetContainsBenchmark {

    @Benchmark
    @OperationsPerInvocation(LookupData.QUERY_COUNT)
    public int hashSet(LookupData data) {
        int found = 0;
        for (int element : data.lookupElements) {
            if (data.hashSet.contains(element)) {
                found++;
            }
        }
        return found;
    }

    @Benchmark
    @OperationsPerInvocation(LookupData.QUERY_COUNT)
    public int bitSet(LookupData data) {
        int found = 0;
        for (int element : data.lookupElements) {
            if (data.bitSet.contains(element)) {
                found++;
            }
        }
        return found;
    }
}

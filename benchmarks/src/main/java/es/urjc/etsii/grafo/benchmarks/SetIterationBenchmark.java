package es.urjc.etsii.grafo.benchmarks;

import es.urjc.etsii.grafo.util.collections.BitSet;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparing iteration performance between HashSet and BitSet.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 3)
@Fork(1)
public class SetIterationBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    @Param({"0.5", "0.9"}) // Fill ratio
    private double fillRatio;

    private HashSet<Integer> hashSet;
    private BitSet bitSet;

    @Setup(Level.Trial)
    public void setup() {
        int capacity = size;
        int numElements = (int) (size * fillRatio);
        Random random = new Random(42);
        
        // Create and populate sets
        hashSet = new HashSet<>(capacity);
        bitSet = new BitSet(capacity);
        
        for (int i = 0; i < numElements; i++) {
            int element = random.nextInt(capacity);
            hashSet.add(element);
            bitSet.add(element);
        }
    }

    @Benchmark
    public void hashSetIteration(Blackhole blackhole) {
        int sum = 0;
        for (Integer element : hashSet) {
            sum += element;
        }
        blackhole.consume(sum);
    }

    @Benchmark
    public void bitSetIteration(Blackhole blackhole) {
        int sum = 0;
        for (Integer element : bitSet) {
            sum += element;
        }
        blackhole.consume(sum);
    }
}

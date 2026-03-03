package es.urjc.etsii.grafo.benchmarks;

import es.urjc.etsii.grafo.util.collections.BitSet;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparing add operation performance between HashSet and BitSet
 * for different data sizes.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 3)
@Fork(1)
public class SetAddBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    @Param({"0.5", "0.9"}) // Fill ratio
    private double fillRatio;

    private int[] elements;
    private int capacity;

    @Setup(Level.Trial)
    public void setup() {
        capacity = size;
        int numElements = (int) (size * fillRatio);
        elements = new int[numElements];
        Random random = new Random(42);
        
        // Generate random unique elements
        for (int i = 0; i < numElements; i++) {
            elements[i] = random.nextInt(capacity);
        }
    }

    @Benchmark
    public void hashSetAdd(Blackhole blackhole) {
        HashSet<Integer> set = new HashSet<>(capacity);
        for (int element : elements) {
            set.add(element);
        }
        blackhole.consume(set);
    }

    @Benchmark
    public void bitSetAdd(Blackhole blackhole) {
        BitSet set = new BitSet(capacity);
        for (int element : elements) {
            set.add(element);
        }
        blackhole.consume(set);
    }
}

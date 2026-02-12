package es.urjc.etsii.grafo.benchmarks;

import es.urjc.etsii.grafo.util.collections.BitSet;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparing contains/lookup operation performance between HashSet and BitSet.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 3)
@Fork(1)
public class SetContainsBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    @Param({"0.5", "0.9"}) // Fill ratio
    private double fillRatio;

    private HashSet<Integer> hashSet;
    private BitSet bitSet;
    private int[] lookupElements;
    private int capacity;

    @Setup(Level.Trial)
    public void setup() {
        capacity = size;
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
        
        // Create lookup elements (mix of existing and non-existing)
        lookupElements = new int[1000];
        for (int i = 0; i < lookupElements.length; i++) {
            lookupElements[i] = random.nextInt(capacity);
        }
    }

    @Benchmark
    public void hashSetContains(Blackhole blackhole) {
        int count = 0;
        for (int element : lookupElements) {
            if (hashSet.contains(element)) {
                count++;
            }
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void bitSetContains(Blackhole blackhole) {
        int count = 0;
        for (int element : lookupElements) {
            if (bitSet.contains(element)) {
                count++;
            }
        }
        blackhole.consume(count);
    }
}

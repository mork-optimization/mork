package es.urjc.etsii.grafo.benchmarks;

import es.urjc.etsii.grafo.util.collections.BitSet;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparing mixed operations (add, contains, remove) between HashSet and BitSet.
 * This simulates realistic workloads where multiple operations are performed.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 3)
@Fork(1)
public class SetMixedOperationsBenchmark {

    @Param({"1000", "10000"})
    private int size;

    private int[] addElements;
    private int[] lookupElements;
    private int[] removeElements;
    private int capacity;

    @Setup(Level.Trial)
    public void setup() {
        capacity = size;
        Random random = new Random(42);
        
        // Generate elements for different operations
        addElements = new int[size];
        lookupElements = new int[size / 2];
        removeElements = new int[size / 4];
        
        for (int i = 0; i < size; i++) {
            addElements[i] = random.nextInt(capacity);
        }
        
        for (int i = 0; i < lookupElements.length; i++) {
            lookupElements[i] = random.nextInt(capacity);
        }
        
        for (int i = 0; i < removeElements.length; i++) {
            removeElements[i] = addElements[i];
        }
    }

    @Benchmark
    public void hashSetMixedOps(Blackhole blackhole) {
        HashSet<Integer> set = new HashSet<>(capacity);
        
        // Add phase
        for (int element : addElements) {
            set.add(element);
        }
        
        // Lookup phase
        int found = 0;
        for (int element : lookupElements) {
            if (set.contains(element)) {
                found++;
            }
        }
        
        // Remove phase
        for (int element : removeElements) {
            set.remove(element);
        }
        
        blackhole.consume(set);
        blackhole.consume(found);
    }

    @Benchmark
    public void bitSetMixedOps(Blackhole blackhole) {
        BitSet set = new BitSet(capacity);
        
        // Add phase
        for (int element : addElements) {
            set.add(element);
        }
        
        // Lookup phase
        int found = 0;
        for (int element : lookupElements) {
            if (set.contains(element)) {
                found++;
            }
        }
        
        // Remove phase
        for (int element : removeElements) {
            set.remove(element);
        }
        
        blackhole.consume(set);
        blackhole.consume(found);
    }
}

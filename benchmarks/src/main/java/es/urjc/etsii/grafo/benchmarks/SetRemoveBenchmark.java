package es.urjc.etsii.grafo.benchmarks;

import es.urjc.etsii.grafo.util.collections.BitSet;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparing remove operation performance between HashSet and BitSet.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 3)
@Fork(1)
public class SetRemoveBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    @Param({"0.5", "0.9"}) // Fill ratio
    private double fillRatio;

    private int[] elementsToRemove;
    private int capacity;
    private int[] initialElements;

    @Setup(Level.Trial)
    public void setup() {
        capacity = size;
        int numElements = (int) (size * fillRatio);
        Random random = new Random(42);
        
        // Generate initial elements
        initialElements = new int[numElements];
        for (int i = 0; i < numElements; i++) {
            initialElements[i] = random.nextInt(capacity);
        }
        
        // Generate elements to remove (half of the initial elements)
        elementsToRemove = new int[numElements / 2];
        for (int i = 0; i < elementsToRemove.length; i++) {
            elementsToRemove[i] = initialElements[i];
        }
    }

    @Benchmark
    public void hashSetRemove(Blackhole blackhole) {
        HashSet<Integer> set = new HashSet<>(capacity);
        for (int element : initialElements) {
            set.add(element);
        }
        for (int element : elementsToRemove) {
            set.remove(element);
        }
        blackhole.consume(set);
    }

    @Benchmark
    public void bitSetRemove(Blackhole blackhole) {
        BitSet set = new BitSet(capacity);
        for (int element : initialElements) {
            set.add(element);
        }
        for (int element : elementsToRemove) {
            set.remove(element);
        }
        blackhole.consume(set);
    }
}

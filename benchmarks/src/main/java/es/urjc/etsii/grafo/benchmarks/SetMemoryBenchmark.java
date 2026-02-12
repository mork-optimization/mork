package es.urjc.etsii.grafo.benchmarks;

import es.urjc.etsii.grafo.util.collections.BitSet;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark measuring memory allocation between HashSet and BitSet.
 * This measures the memory impact through allocation rate tracking.
 */
@BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 3)
@Fork(value = 1, jvmArgsAppend = {"-XX:+UseG1GC", "-Xms2g", "-Xmx2g"})
public class SetMemoryBenchmark {

    @Param({"1000", "10000", "100000"})
    private int size;

    @Param({"0.5", "0.9"}) // Fill ratio
    private double fillRatio;

    private int[] elements;
    private int capacity;

    @Setup(Level.Invocation)
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
    public void hashSetMemory(Blackhole blackhole) {
        HashSet<Integer> set = new HashSet<>(capacity);
        for (int element : elements) {
            set.add(element);
        }
        // Perform operations to ensure memory is actually allocated
        for (int element : elements) {
            set.contains(element);
        }
        blackhole.consume(set);
    }

    @Benchmark
    public void bitSetMemory(Blackhole blackhole) {
        BitSet set = new BitSet(capacity);
        for (int element : elements) {
            set.add(element);
        }
        // Perform operations to ensure memory is actually allocated
        for (int element : elements) {
            set.contains(element);
        }
        blackhole.consume(set);
    }
}

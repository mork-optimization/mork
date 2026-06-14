package es.urjc.etsii.grafo.benchmarks;

import es.urjc.etsii.grafo.util.collections.BitSet;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.Arrays;
import java.util.HashSet;
import java.util.SplittableRandom;

import static org.openjdk.jmh.annotations.Level.Trial;
import static org.openjdk.jmh.annotations.Scope.Thread;

@State(Thread)
public class BenchmarkData {

    private static final double HASH_SET_LOAD_FACTOR = 0.75d;

    @Param({"1024", "16384", "262144"})
    public int universeSize;

    @Param({"0.01", "0.10", "0.50", "0.90"})
    public double fillRatio;

    public int cardinality;
    public int[] presentElements;
    public int[] absentElements;
    public HashSet<Integer> hashSet;
    public BitSet bitSet;

    @Setup(Trial)
    public void setup() {
        this.cardinality = cardinality(universeSize, fillRatio);

        int[] universe = shuffledUniverse(universeSize);
        this.presentElements = Arrays.copyOfRange(universe, 0, cardinality);
        this.absentElements = Arrays.copyOfRange(universe, cardinality, universe.length);

        this.hashSet = newHashSet(presentElements);
        this.bitSet = newBitSet(presentElements);
    }

    public HashSet<Integer> newHashSet(int[] elements) {
        var set = sizedHashSet(elements.length);
        for (int element : elements) {
            set.add(element);
        }
        return set;
    }

    public BitSet newBitSet(int[] elements) {
        var set = new BitSet(universeSize);
        for (int element : elements) {
            set.add(element);
        }
        return set;
    }

    private static int cardinality(int universeSize, double fillRatio) {
        int cardinality = (int) Math.round(universeSize * fillRatio);
        return Math.max(1, Math.min(cardinality, universeSize - 1));
    }

    private static HashSet<Integer> sizedHashSet(int expectedSize) {
        int capacity = (int) Math.ceil(expectedSize / HASH_SET_LOAD_FACTOR) + 1;
        return new HashSet<>(Math.max(16, capacity));
    }

    private static int[] shuffledUniverse(int universeSize) {
        int[] values = new int[universeSize];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }

        var random = new SplittableRandom(42);
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
        return values;
    }
}

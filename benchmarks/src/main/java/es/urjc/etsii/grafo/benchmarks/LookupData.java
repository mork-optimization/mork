package es.urjc.etsii.grafo.benchmarks;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.SplittableRandom;

import static org.openjdk.jmh.annotations.Level.Trial;
import static org.openjdk.jmh.annotations.Scope.Thread;

@State(Thread)
public class LookupData extends BenchmarkData {

    public static final int QUERY_COUNT = 4096;

    @Param({"0.00", "0.50", "1.00"})
    public double hitRatio;

    public int[] lookupElements;

    @Override
    @Setup(Trial)
    public void setup() {
        super.setup();

        int hits = (int) Math.round(QUERY_COUNT * hitRatio);
        this.lookupElements = new int[QUERY_COUNT];
        for (int i = 0; i < hits; i++) {
            lookupElements[i] = presentElements[i % presentElements.length];
        }
        for (int i = hits; i < lookupElements.length; i++) {
            lookupElements[i] = absentElements[i % absentElements.length];
        }
        shuffle(lookupElements);
    }

    private static void shuffle(int[] values) {
        var random = new SplittableRandom(84);
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
    }
}

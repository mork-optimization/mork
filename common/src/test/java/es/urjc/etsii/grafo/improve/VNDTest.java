package es.urjc.etsii.grafo.improve;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class VNDTest {

    Improver<TestSolution, TestInstance> improver1;
    Improver<TestSolution, TestInstance> improver2;
    Improver<TestSolution, TestInstance> improver3;

    TestSolution solution;
    TestInstance instance;

    private FMode fmode;
    private List<Integer> calls;
    private List<Integer> expectedCallOrder = List.of(1,2,1,1,2,3,1,2,3);

    @BeforeAll
    public static void init(){
        Metrics.disableMetrics();
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    void prepareMocks(){
        calls = new ArrayList<>();
        instance = new TestInstance("vndtest");
        solution = new TestSolution(instance, 6);

        // Improver 1 decreases by 1 if score ==5, improver 2 same but when 6, improver 3 decreases 1 if even
        // Check that in VND, call order is
        // I1 --> I2 --> I1 --> I1 --> I2 --> I3 --> I1 --> I2 --> I3 --> END

        // If maximizing, order instead should be I1 --> I2 --> I3 --> END
        this.improver1 = mock(Improver.class);
        when(improver1.improve(any())).thenAnswer(a -> {
            calls.add(1);
            TestSolution s = a.getArgument(0);
            if(s.getScore() == 5){
                s.setScore(4);
            } else if (s.getScore() == 7){
                s.setScore(8);
            }
            return s;
        });

        this.improver2 = mock(Improver.class);
        when(improver2.improve(any())).thenAnswer(a -> {
            calls.add(2);
            TestSolution s = a.getArgument(0);
            if(s.getScore() == 6){
                s.setScore(fmode == FMode.MINIMIZE? 5: 7);
            }
            return s;
        });
        this.improver3 = mock(Improver.class);
        when(improver3.improve(any())).thenAnswer(a -> {
            calls.add(3);
            TestSolution s = a.getArgument(0);
            if(s.getScore() % 2 == 0){
                s.setScore(s.getScore() + (fmode == FMode.MINIMIZE? -1: +1));
            }
            return s;
        });
    }

    @Test
    void testVNDminimizing(){
        fmode = FMode.MINIMIZE;
        var vnd = new VND<>(FMode.MINIMIZE, improver1, improver2, improver3);
        assertEquals(6, solution.getScore());
        var improvedSolution = vnd.improve(solution);
        assertEquals(3, improvedSolution.getScore());

        assertEquals(expectedCallOrder, calls);
    }

    @Test
    void testVNDmaximizing(){
        fmode = FMode.MAXIMIZE;
        var vnd = new VND<>(FMode.MAXIMIZE, improver1, improver2, improver3);
        assertEquals(6, solution.getScore());
        var improvedSolution = vnd.improve(solution);
        assertEquals(9, improvedSolution.getScore());

        assertEquals(expectedCallOrder, calls);
    }
}

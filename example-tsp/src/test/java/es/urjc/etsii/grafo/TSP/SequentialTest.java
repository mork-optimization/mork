package es.urjc.etsii.grafo.TSP;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
        properties = {
                "solver.parallelExecutor=false",
                "solver.repetitions=100"
        }
)
public class SequentialTest {
}

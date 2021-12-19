package es.urjc.etsii.grafo.TSP;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
        properties = {
                "solver.parallelExecutor=true",
                "solver.repetitions=100",
                "solver.nWorkers=-1"
        }
)
public class ConcurrentTest {
}

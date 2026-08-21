package es.urjc.etsii.grafo.graphs.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.cmsa.CMSABuilder;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.graphs.model.MSTInstance;
import es.urjc.etsii.grafo.graphs.model.MSTSolution;
import es.urjc.etsii.grafo.graphs.mvc.MVCConstructive;
import es.urjc.etsii.grafo.graphs.mvc.MVCExactCoverSolver;

import java.util.List;

/**
 * Minimum Vertex Cover (MVC), solved with CMSA (Construct, Merge, Solve &amp; Adapt).
 * Unlike MST/Shortest Paths, MVC is NP-hard: there is no known polynomial exact algorithm,
 * which is exactly the kind of problem CMSA targets. See {@link MVCConstructive} for the
 * probabilistic construction used to sample candidate vertices, and {@link MVCExactCoverSolver}
 * for the exact method used to solve the restricted sub-instance at every iteration.
 */
public class MinVertexCoverExp extends AbstractExperiment<MSTSolution, MSTInstance> {

    @Override
    public List<Algorithm<MSTSolution, MSTInstance>> getAlgorithms() {
        var cmsa = new CMSABuilder<MSTSolution, MSTInstance>()
                .withDefaultObjective()
                .withConstructive(new MVCConstructive())
                .withSolver(new MVCExactCoverSolver())
                .withSolutionsPerIteration(20)
                .withAgeMax(5)
                .withSolverTimeLimitInMillis(200)
                .withMaxIterations(200)
                .build("CMSA-MVC");

        return List.of(cmsa);
    }
}

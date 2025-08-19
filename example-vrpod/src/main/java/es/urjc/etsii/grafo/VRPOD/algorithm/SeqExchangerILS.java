package es.urjc.etsii.grafo.VRPOD.algorithm;

import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.util.TimeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("DuplicatedCode")
public class SeqExchangerILS extends Algorithm<VRPODSolution, VRPODInstance> {

    private static final Logger log = LoggerFactory.getLogger(SeqExchangerILS.class);
    private final int nSoluciones;
    private final int nRotateRounds;
    private final ILSConfig[] configs;

    /**
     * Create a new MultiStartAlgorithm, @see algorithm
     */
    public SeqExchangerILS(String name, int nSoluciones, int nRotateRounds, ILSConfig... configs) {
        super(name);
        this.nSoluciones = nSoluciones;
        this.nRotateRounds = nRotateRounds;
        this.configs = configs;
    }

    /**
     * Executes the algorythm for the given instance
     *
     * @param ins Instance the algorithm will process
     * @return Best solution found
     */
    public VRPODSolution algorithm(VRPODInstance ins) {
        var nWorkers = this.configs.length;
        VRPODSolution best = null;
        // Create threads and workers
        var workers = new ILSWorker[nWorkers];
        var currentSolutions = new VRPODSolution[nWorkers];

        for (int i = 0; i < nWorkers; i++) {
            workers[i] = new ILSWorker(i, currentSolutions, getBuilder(), configs[i], nWorkers, nRotateRounds);
        }

        if (nSoluciones % nWorkers != 0) {
            log.warn("nSolutions is not a multiple of workers, using nearest number: {}", nSoluciones / nWorkers * nWorkers);
        }

        for (int i = 0; i < nSoluciones / nWorkers; i++) {
            for (int j = 0; j < workers.length && !TimeControl.isTimeUp(); j++) {
                currentSolutions[j] = workers[j].buildInitialSolution(ins);
            }

            // Improvement rounds
            for (int j = 0; j < workers.length && !TimeControl.isTimeUp(); j++) {
                ILSWorker worker = workers[j];
                worker.work();
            }

            // Migrate solutions
            var first = currentSolutions[0];
            for (int j = 1; j < currentSolutions.length; j++) {
                currentSolutions[j - 1] = currentSolutions[j];
            }
            currentSolutions[currentSolutions.length - 1] = first;
            best = getBestSolution(currentSolutions);
        }

        return best;
    }

    private static VRPODSolution getBestSolution(VRPODSolution[] solutions) {
        double min = Double.MAX_VALUE;
        VRPODSolution best = null;
        for (var solution : solutions) {
            if (solution != null && solution.getOptimalValue() < min) {
                best = solution;
                min = solution.getOptimalValue();
            }
        }
        return best;
    }
}

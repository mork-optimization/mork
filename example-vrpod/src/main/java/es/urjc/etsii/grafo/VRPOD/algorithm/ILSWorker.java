package es.urjc.etsii.grafo.VRPOD.algorithm;

import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.util.TimeControl;

public class ILSWorker {

    private final int id;

    private final VRPODSolution[] solutions;
    private final SolutionBuilder<VRPODSolution, VRPODInstance> builder;
    private final ILSConfig config;
    private final int nWorkers;
    private final int nRotaterounds;

    public ILSWorker(int id, VRPODSolution[] solutions, SolutionBuilder<VRPODSolution, VRPODInstance> builder, ILSConfig config, int nWorkers, int nRotaterounds) {
        this.id = id;
        this.solutions = solutions;
        this.builder = builder;
        this.config = config;
        this.nWorkers = nWorkers;
        this.nRotaterounds = nRotaterounds;
    }

    public VRPODSolution buildInitialSolution(VRPODInstance instance){
        var solution = this.builder.initializeSolution(instance);
        return config.constructor.construct(solution);
    }

    public void work() {
        var nShakes = this.config.nShakes == -1? solutions[id].ins.getRecommendedNumberOfShakes(): this.config.nShakes;
        var numeroRebotesDeLaSolucion = this.nWorkers * nRotaterounds;
        var currentNShakes = nShakes / numeroRebotesDeLaSolucion; // Iteraciones de shake que tenemos que hacer por cada ronda

        // Numero de veces que tiene que hacer pull/push
        for (int round = 0; round < numeroRebotesDeLaSolucion; round++) {
            for (int i = 1; i <= currentNShakes && !TimeControl.isTimeUp(); i++) {
                solutions[id] = iteration(solutions[id]);
            }
        }
    }

    private VRPODSolution iteration(VRPODSolution best) {
        var current = best.cloneSolution();
        this.config.shake.shake(current, this.config.shakeStrength);
        this.config.improver.improve(current);
        if (current.getOptimalValue() < best.getOptimalValue()) {
            best = current;
        }
        return best;
    }
}

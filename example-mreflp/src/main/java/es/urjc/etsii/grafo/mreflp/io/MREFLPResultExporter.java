package es.urjc.etsii.grafo.mreflp.io;

import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;
import es.urjc.etsii.grafo.mreflp.MREFLPConfig;
import es.urjc.etsii.grafo.mreflp.alg.LMLSParameters;
import es.urjc.etsii.grafo.mreflp.model.*;
import tools.jackson.databind.json.JsonMapper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;

/** Atomic, per-run checkpoints. Export and independent validation are outside measured algorithm time. */
public class MREFLPResultExporter extends SolutionSerializer<MREFLPSolution, MREFLPInstance> {
    private final MREFLPConfig problem;
    private final SolverConfig solver;
    public MREFLPResultExporter(MREFLPResultConfig config, MREFLPConfig problem, SolverConfig solver) {
        super(config);
        this.problem = problem;
        this.solver = solver;
    }
    @Override protected String getFilename(String experiment, String instance, String algorithm, String iteration) {
        if (!iteration.matches("\\d+")) return "summary.json";
        return instance + "-" + algorithm + "-" + (solver.getSeed() + Integer.parseInt(iteration)) + ".run.json";
    }
    private RunRecord record(WorkUnitResult<MREFLPSolution, MREFLPInstance> r) {
        new MREFLPSolutionValidator().validate(r.solution()).throwIfFail();
        return new RunRecord(1, r.instanceId(), r.algorithm().getName(), problem.getProtocol(),
                solver.getSeed() + Integer.parseInt(r.iteration()), problem.getTimeLimitSeconds(), problem.getMaxRestarts(),
                LMLSParameters.PAPER, solver.getRandomType().getJavaName(), r.solution().getInstance().sourceHash(),
                System.getProperty("mreflp.artifact-hash", "unrecorded"), r.solution().cost(), r.executionTime(), r.timeToTarget(),
                r.solution().assignments(), System.getProperty("java.runtime.version"), System.getProperty("java.vm.name"),
                System.getProperty("os.name"), System.getProperty("os.arch"), Runtime.getRuntime().availableProcessors(),
                solver.getWarmup().isEnabled(), solver.getWarmup().getRepetitions(), solver.getWarmup().getMaxMillis());
    }
    @Override public void export(String folder, String suggestedFilename, WorkUnitResult<MREFLPSolution, MREFLPInstance> result) {
        // The framework also exports aggregate winners; only individual runs are checkpoints.
        if (!result.iteration().matches("\\d+")) return;
        Path target = Path.of(folder).resolve(suggestedFilename);
        try {
            AtomicFileUtil.write(target, JsonMapper.builder().build().writerWithDefaultPrettyPrinter().writeValueAsString(record(result)));
        } catch (IOException e) { throw new IllegalStateException("Cannot checkpoint " + target, e); }
    }
    @Override public void export(BufferedWriter writer, WorkUnitResult<MREFLPSolution, MREFLPInstance> result) throws IOException {
        writer.write(JsonMapper.builder().build().writeValueAsString(record(result)));
    }
}

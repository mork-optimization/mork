package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.io.*;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.ExecutionJournalUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@ConditionalOnProperty(name = "autollm.child.comparison-enabled", havingValue = "true")
public class AutoLlmComparisonExperiment<S extends Solution<S,I>, I extends Instance> extends AbstractExperiment<S,I> {
    private final AutomaticAlgorithmBuilder<S,I> builder;
    private final InstanceManager<I> instances;
    private final Environment environment;
    public AutoLlmComparisonExperiment(AutomaticAlgorithmBuilder<S,I> builder, InstanceManager<I> instances, Environment environment) {
        this.builder = builder; this.instances = instances; this.environment = environment;
    }
    @Override public String getName() { return "autollm-comparison"; }
    @Override public List<Algorithm<S,I>> getAlgorithms() {
        try {
            var config = AutoLlmFileUtil.read(Path.of(environment.getRequiredProperty("autollm.child.config-file")), AutoLlmConfig.class);
            var sota = AutoLlmComparisonUtil.restart("SOTA", builder.buildFromJson(Files.readString(Path.of(config.sotaFile()))));
            var proposal = AutoLlmComparisonUtil.restart("IraceProposal", builder.buildFromJson(Files.readString(Path.of(environment.getRequiredProperty("autollm.child.winner-file")))));
            var budgets = AutoLlmFileUtil.read(Path.of(environment.getRequiredProperty("autollm.child.time-limits-file")), Map.class);
            for (String path : instances.getInstanceSolveOrder(getName(), false)) {
                if (!(budgets.get(path) instanceof Number time) || time.longValue() <= 0) throw new IllegalStateException("Missing frozen comparison time budget: " + path);
            }
            ExecutionJournalUtil.write("time-limits.json", budgets);
            System.setProperty("mork.execution.time-limits", ExecutionJournalUtil.directory().resolve("time-limits.json").toString());
            return List.of(sota, proposal);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
}

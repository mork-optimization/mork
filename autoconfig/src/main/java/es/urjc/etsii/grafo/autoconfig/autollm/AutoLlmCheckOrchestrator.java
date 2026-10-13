package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.autoconfig.builder.*;
import es.urjc.etsii.grafo.autoconfig.irace.*;
import es.urjc.etsii.grafo.autoconfig.service.*;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceExecuteConfig;
import es.urjc.etsii.grafo.config.*;
import es.urjc.etsii.grafo.io.*;
import es.urjc.etsii.grafo.orchestrator.AbstractOrchestrator;
import es.urjc.etsii.grafo.services.ExecutionLifecycleCoordinator;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.solution.*;
import es.urjc.etsii.grafo.util.*;
import org.springframework.core.env.Environment;
import java.io.*;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;

/** Child-only metadata checks, targeted seed/component probes, and winner export. */
public class AutoLlmCheckOrchestrator<S extends Solution<S,I>, I extends Instance> extends AbstractOrchestrator {
    public record Check(int parameters, List<String> added, List<String> training, List<String> comparison) {}

    private final Environment environment;
    private final AutoconfigSearchSpace space;
    private final AutomaticAlgorithmBuilder<S,I> builder;
    private final InstanceManager<I> instances;
    private final IraceTargetEvaluator<S,I> evaluator;
    private final AutoconfigRunState state;
    private final SolverConfig solver;
    private final BlockConfig blocks;
    private final IraceConfig irace;
    private final Optional<TimeLimitCalculator<S,I>> limits;
    private final Optional<SolutionValidator<S,I>> validator;
    private final ExecutionLifecycleCoordinator lifecycle;
    public AutoLlmCheckOrchestrator(Environment environment, AutoconfigSearchSpace space, AutomaticAlgorithmBuilder<S,I> builder,
            InstanceManager<I> instances, IraceTargetEvaluator<S,I> evaluator, AutoconfigRunState state, SolverConfig solver,
            BlockConfig blocks, IraceConfig irace, Optional<TimeLimitCalculator<S,I>> limits, Optional<SolutionValidator<S,I>> validator, ExecutionLifecycleCoordinator lifecycle) {
        this.environment = environment; this.space = space; this.builder = builder; this.instances = instances; this.evaluator = evaluator;
        this.state = state; this.solver = solver; this.blocks = blocks; this.irace = irace; this.limits = limits; this.validator = validator; this.lifecycle = lifecycle;
    }
    @Override public List<String> getNames() { return List.of("autollm-check", "autollm-probe", "autollm-export"); }
    @Override public es.urjc.etsii.grafo.monitoring.MonitorModeState.Mode getMode() { return es.urjc.etsii.grafo.monitoring.MonitorModeState.Mode.UTILITY; }
    @Override public void run(String... args) {
        var outcome = es.urjc.etsii.grafo.events.types.ExecutionEndedEvent.Outcome.SUCCEEDED;
        try {
            if (!ExecutionJournalUtil.enabled()) throw new IllegalStateException("AutoLLM checks require a child execution directory");
            if (validator.isEmpty()) throw new IllegalStateException("AutoLLM requires a solution validator");
            Context.Configurator.setSolverConfig(solver); Context.Configurator.setBlockConfig(blocks);
            ExecutionJournalUtil.write("readiness.json", Map.of("startedAtMillis", System.currentTimeMillis(), "pid", ProcessHandle.current().pid(), "port", environment.getProperty("local.server.port", Integer.class, 0)));
            var config = AutoLlmFileUtil.read(Path.of(environment.getRequiredProperty("autollm.child.config-file")), AutoLlmConfig.class);
            for (String arg : args) if (arg.equals("--autollm-export")) { exportWinner(); return; }
            var codec = new ComponentSpecJsonCodec();
            var seeds = codec.parseList(Files.readString(Path.of(irace.getInitialConfigurationsFile())));
            InitialConfigurationUtil.toIraceTable(space, seeds);
            var components = new TreeSet<String>();
            for (String pkg : environment.getProperty("advanced.scan-pkgs", "es.urjc.etsii").split(",")) {
                for (var type : ReflectionUtil.findTypesByAnnotation(pkg, AlgorithmComponent.class)) {
                    if (Modifier.isPublic(type.getModifiers()) && !Modifier.isAbstract(type.getModifiers())) components.add(type.getName());
                }
            }
            List<String> added = new ArrayList<>();
            String baselineFile = environment.getProperty("autollm.child.baseline-components-file");
            if (baselineFile != null) {
                var baseline = new HashSet<>(List.of(AutoLlmFileUtil.read(Path.of(baselineFile), String[].class)));
                added = AutoLlmComponentUtil.validate(components, baseline, space.iraceParameters().size(), config.maxParameters());
            }
            var training = instances.getInstanceSolveOrder("irace", false);
            var comparison = instances.getInstanceSolveOrder("autollm-comparison", false);
            if (training.isEmpty() || comparison.isEmpty()) throw new IllegalStateException("Training and comparison inputs must be nonempty");
            if (config.comparison().timeLimitMillis() <= 0 && limits.isEmpty()) throw new IllegalStateException("Comparison needs a time limit calculator or explicit limit");
            if (baselineFile == null) {
                var sota = AutoLlmComparisonUtil.restart("SOTA", builder.buildFromJson(codec.toJson(seeds.getFirst())));
                var budgets = new LinkedHashMap<String,Long>();
                for (String path : comparison) {
                    long budget = config.comparison().timeLimitMillis() > 0 ? config.comparison().timeLimitMillis() : limits.orElseThrow().timeLimitInMillis(instances.getInstance(path), sota);
                    if (budget <= 0) throw new IllegalStateException("Comparison time budget must be positive: " + path);
                    budgets.put(path, budget);
                }
                ExecutionJournalUtil.write("comparison-time-limits.json", budgets);
            }
            ExecutionJournalUtil.write("components.json", components);
            ExecutionJournalUtil.write("search-space.json", space.snapshot());
            ExecutionJournalUtil.write("check.json", new Check(space.iraceParameters().size(), added, training, comparison));
            for (String arg : args) if (arg.equals("--autollm-probe")) probe(seeds, added, training.getFirst(), codec);
        } catch (IOException e) { outcome = es.urjc.etsii.grafo.events.types.ExecutionEndedEvent.Outcome.FAILED; throw new UncheckedIOException(e); }
        catch (RuntimeException | Error e) { outcome = es.urjc.etsii.grafo.events.types.ExecutionEndedEvent.Outcome.FAILED; throw e; }
        finally { lifecycle.complete(0, outcome); }
    }
    private void exportWinner() throws IOException {
        Path file = Path.of(environment.getRequiredProperty("autollm.child.elites-file"));
        var elite = IraceFinalElitesUtil.read(file).getFirst();
        String json = builder.asJson(new AlgorithmConfiguration(elite.parameters()));
        builder.buildFromJson(json);
        ExecutionJournalUtil.write("winner.json", AutoLlmFileUtil.JSON.readTree(json));
        ExecutionJournalUtil.write("winner-parameters.json", elite);
    }
    private void probe(List<ComponentSpec> seeds, List<String> added, String instance, ComponentSpecJsonCodec codec) {
        state.prepareCoordinator(AutoconfigRunState.RunMode.AUTOCONFIG, IraceCostMetricUtil.describe(AutoconfigRunState.RunMode.AUTOCONFIG, solver, irace));
        var specs = new ArrayList<>(seeds);
        var names = new ArrayList<String>();
        for (int i = 0; i < seeds.size(); i++) names.add("seed-" + i);
        var skipped = new LinkedHashMap<String, String>();
        for (String component : added) {
            try { specs.add(AutoLlmProbeUtil.sample(space, component)); names.add(component); }
            catch (IllegalArgumentException e) { skipped.put(component, e.getMessage()); }
        }
        var outcomes = new LinkedHashMap<String, String>();
        for (int i = 0; i < specs.size(); i++) {
            String[] table = InitialConfigurationUtil.toIraceTable(space, List.of(specs.get(i))).split("\n");
            String[] headers = table[0].split("\t"); String[] cells = table[1].split("\t");
            var parameters = new LinkedHashMap<String, String>();
            for (int j = 0; j < headers.length; j++) {
                String value = cells[j];
                if (value.startsWith("\"")) value = value.substring(1, value.length() - 1).replace("\\\"", "\"");
                parameters.put(headers[j], value);
            }
            var response = evaluator.preflightBatch(List.of(IraceExecuteConfig.of(names.get(i), 1, instance, (int) solver.getSeed(), parameters))).getFirst();
            outcomes.put(names.get(i), response.toIraceResultString());
            ExecutionJournalUtil.write("probes.json", Map.of("outcomes", outcomes, "skipped", skipped));
        }
        for (int i = 0; i < seeds.size(); i++) if (outcomes.get("seed-" + i).startsWith("Inf")) throw new IllegalStateException("Supplied seed did not execute successfully: " + i);
        // Feasibility failures and invalid combinations are normal penalties during tuning.
        // An exception from a generated component is actionable repair feedback.
        for (var event : AutoLlmFileUtil.events(ExecutionJournalUtil.directory().resolve("events.jsonl"))) {
            if ("evaluation-result".equals(event.get("kind")) && added.contains(event.get("configurationId"))
                    && "CANDIDATE_EXCEPTION".equals(event.get("rejectionCode"))) {
                throw new IllegalStateException("Generated component probe raised an exception: " + event.get("configurationId") + "; " + event.get("rejectionMessage"));
            }
        }
    }
}

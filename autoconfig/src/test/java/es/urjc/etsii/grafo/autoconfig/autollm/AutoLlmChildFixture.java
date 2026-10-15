package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.ExecutionJournalUtil;
import java.nio.file.*;
import java.util.*;

/** Portable fake child protocol for lifecycle tests; actual irace is tested separately. */
public class AutoLlmChildFixture {
    public static void main(String[] args) throws Exception {
        var flags = new HashMap<String,String>();
        for (String arg : args) { int separator = arg.indexOf('='); if (separator > 0) flags.put(arg.substring(0, separator), arg.substring(separator + 1)); }
        Path directory = ExecutionJournalUtil.directory();
        boolean assertions = false;
        assert assertions = true;
        ExecutionJournalUtil.write("assertions.json", Map.of("application", assertions,
                "moves", Move.class.desiredAssertionStatus(), "solutions", Solution.class.desiredAssertionStatus()));
        ExecutionJournalUtil.write("readiness.json", Map.of("startedAtMillis", System.currentTimeMillis(), "pid", ProcessHandle.current().pid()));
        if (Arrays.asList(args).contains("--autollm-check")) {
            var training = Files.readAllLines(Path.of(flags.get("--instances.path.irace")));
            var comparison = Files.readAllLines(Path.of(flags.get("--instances.path.autollm-comparison")));
            ExecutionJournalUtil.write("components.json", List.of("Dummy")); ExecutionJournalUtil.write("search-space.json", Map.of("parameters", 2));
            ExecutionJournalUtil.write("check.json", Map.of("training", training, "comparison", comparison, "parameters", 2, "added", List.of()));
            var budgets = new LinkedHashMap<String,Long>(); for (String path : comparison) budgets.put(path, 20L);
            ExecutionJournalUtil.write("comparison-time-limits.json", budgets);
        } else if (Arrays.asList(args).contains("--autollm-probe")) {
            ExecutionJournalUtil.write("probes.json", Map.of("outcomes", Map.of("seed-0", "1 0.01"), "skipped", Map.of()));
            ExecutionJournalUtil.finished("probe", Map.of("state", "SUCCEEDED", "preflight", true));
        } else if (Arrays.asList(args).contains("--autollm-export")) {
            ExecutionJournalUtil.write("winner.json", Map.of("$component", "Dummy")); ExecutionJournalUtil.write("winner-parameters.json", Map.of("ROOT", "Dummy"));
        } else if (Arrays.asList(args).contains("--default")) {
            int repetitions = Integer.parseInt(flags.get("--solver.repetitions")); long seed = Long.parseLong(flags.get("--solver.seed"));
            String instance = Files.readAllLines(Path.of(flags.get("--instances.path.autollm-comparison"))).getFirst();
            for (String name : List.of("SOTA", "IraceProposal")) for (int i = 0; i < repetitions; i++) {
                var row = new LinkedHashMap<String,Object>(); row.put("id", UUID.randomUUID().toString()); row.put("state", "SUCCEEDED"); row.put("measured", true); row.put("algorithm", name);
                row.put("iteration", Integer.toString(i)); row.put("seed", seed + i); row.put("instance", instance); row.put("instanceId", "instance.txt"); row.put("objectives", Map.of("Test", name.equals("SOTA") ? 10 : 9));
                row.put("properties", Map.of()); row.put("executionTimeNanos", 10_000_000); row.put("timeToBestNanos", 1_000_000); row.put("curves", Map.of("Test", List.of(Map.of("instant", 0, "value", 10))));
                ExecutionJournalUtil.finished((String) row.get("id"), row);
            }
            Files.createDirectories(Path.of("results")); Files.writeString(Path.of("results/comparison.xlsx"), "fake fixture workbook");
        } else if (Arrays.asList(args).contains("--autoconfig")) {
            if (directory.toString().contains("/001/") && directory.toString().contains("-tuning/")) {
                ExecutionJournalUtil.phase("hung", "RUNNING", 20, Map.of());
                while (true) { ExecutionJournalUtil.append("worker-progress", Map.of("anotherWorker", "alive")); Thread.sleep(100); }
            }
            ExecutionJournalUtil.finished("evaluation", Map.of("state", "SUCCEEDED", "preflight", false, "parameters", Map.of("ROOT", "Dummy")));
            Files.writeString(Path.of("autoconfig-final-elites.json"), "{\"elites\":[{\"configurationId\":\"1\",\"parameters\":{\"ROOT\":\"Dummy\"}}]}");
        } else if (Arrays.asList(args).contains("hang")) {
            ExecutionJournalUtil.phase("hung", "RUNNING", 20, Map.of());
            while (true) { ExecutionJournalUtil.append("worker-progress", Map.of("alive", true)); Thread.sleep(100); }
        } else if (Arrays.asList(args).contains("pilot")) {
            while (!Files.exists(directory.resolve("stop"))) Thread.sleep(10);
        }
        ExecutionJournalUtil.terminal("SUCCEEDED");
    }
}

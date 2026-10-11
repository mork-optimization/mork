package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.orchestrator.AbstractOrchestrator;
import es.urjc.etsii.grafo.config.BlockConfig;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.monitoring.MonitorModeState;
import es.urjc.etsii.grafo.util.Context;
import java.util.List;

/** The parent remains available for UI-driven campaigns and does not complete a child lifecycle. */
public class AutoLlmOrchestrator extends AbstractOrchestrator {
    private final SolverConfig solver;
    private final BlockConfig blocks;
    private final AutoLlmCampaignService campaigns;
    private final org.springframework.core.env.Environment environment;
    public AutoLlmOrchestrator(SolverConfig solver, BlockConfig blocks, AutoLlmCampaignService campaigns, org.springframework.core.env.Environment environment) { this.solver = solver; this.blocks = blocks; this.campaigns = campaigns; this.environment = environment; }
    @Override public List<String> getNames() { return List.of("autollm"); }
    @Override public MonitorModeState.Mode getMode() { return MonitorModeState.Mode.AUTOLLM; }
    @Override public void run(String... args) {
        Context.Configurator.setSolverConfig(solver); Context.Configurator.setBlockConfig(blocks); campaigns.recover();
        es.urjc.etsii.grafo.util.ExecutionJournalUtil.write("readiness.json", java.util.Map.of("startedAtMillis", System.currentTimeMillis(), "pid", ProcessHandle.current().pid(), "port", environment.getProperty("local.server.port", Integer.class, 0)));
    }
}

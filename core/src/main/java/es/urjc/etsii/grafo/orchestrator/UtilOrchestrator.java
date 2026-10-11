package es.urjc.etsii.grafo.orchestrator;

import es.urjc.etsii.grafo.monitoring.MonitorModeState.Mode;

public class UtilOrchestrator extends AbstractOrchestrator {

    @Override
    public Mode getMode() {
        return Mode.UTILITY;
    }

    @Override
    public void run(String... args) {
        throw new UnsupportedOperationException("Util orchestrator not implemented yet");
    }
}

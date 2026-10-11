package es.urjc.etsii.grafo.monitoring;

import org.springframework.stereotype.Service;

/** Mode selected by the core runner, independent of optional autoconfig endpoints. */
@Service
public class MonitorModeState {
    public enum Mode { DETECTING, STANDARD, TUNING, WORKER, UTILITY, AUTOLLM }
    private volatile Mode mode = Mode.DETECTING;
    public Mode mode() { return mode; }
    public void select(Mode mode) { this.mode = mode; }
}

package es.urjc.etsii.grafo.restcontroller;

import es.urjc.etsii.grafo.events.InMemoryEventLog;
import es.urjc.etsii.grafo.monitoring.MonitorModeState;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@org.springframework.web.bind.annotation.CrossOrigin(origins = "http://localhost:4200")
public class MonitorController {
    private final MonitorModeState mode;
    private final InMemoryEventLog events;
    public MonitorController(MonitorModeState mode, InMemoryEventLog events) { this.mode = mode; this.events = events; }
    public record Status(MonitorModeState.Mode mode, String eventLogId) {}
    @GetMapping("/api/monitor/status")
    public Status status() { return new Status(mode.mode(), events.eventLogId()); }
}

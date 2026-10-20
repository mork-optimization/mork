package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmLogUtil;
import es.urjc.etsii.grafo.monitoring.MonitorModeState;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@ConditionalOnProperty("autollm")
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/autollm/screenings")
public class ScreeningJobController {
    private final ScreeningJobService service;
    private final MonitorModeState mode;
    public ScreeningJobController(ScreeningJobService service, MonitorModeState mode) { this.service = service; this.mode = mode; }
    private void requireMode() { if (mode.mode() != MonitorModeState.Mode.AUTOLLM) throw new ResponseStatusException(HttpStatus.CONFLICT, "Launch using --autollm"); }
    @GetMapping public Object list() { return Map.of("active", service.active() == null ? "" : service.active(), "jobs", service.list()); }
    @PostMapping public Object create(@RequestBody ScreeningJobService.Request request) { requireMode(); return service.create(request); }
    @GetMapping("/{id}") public Object get(@PathVariable String id) { return service.load(id); }
    @PostMapping("/{id}/resume") public Object resume(@PathVariable String id) { requireMode(); return service.resume(id); }
    @PostMapping("/{id}/cancel") public Object cancel(@PathVariable String id) { requireMode(); return service.cancel(id); }
    @PostMapping("/{id}/approve") public Object approve(@PathVariable String id, @RequestBody Map<String,String> review) { requireMode(); return service.approve(id, review.get("proposalSha256")); }
    @GetMapping("/{id}/review") public Object review(@PathVariable String id) { return service.review(id); }
    @GetMapping("/{id}/artifact") public ResponseEntity<FileSystemResource> artifact(@PathVariable String id, @RequestParam String path) {
        var file = service.artifact(id, path);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.getFileName().toString()).build().toString()).body(new FileSystemResource(file));
    }
    @GetMapping("/{id}/log") public Object log(@PathVariable String id, @RequestParam String path, @RequestParam(defaultValue = "0") long cursor) {
        if (!(path.endsWith(".log") || path.endsWith(".jsonl") || path.endsWith(".txt"))) throw new IllegalArgumentException("Select a text log");
        return AutoLlmLogUtil.read(service.artifact(id, path), cursor, 65536);
    }
    @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<?> invalid(IllegalArgumentException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    @ExceptionHandler(IllegalStateException.class) public ResponseEntity<?> conflict(IllegalStateException e) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage())); }
    @ExceptionHandler(NoSuchElementException.class) public ResponseEntity<?> missing(NoSuchElementException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage())); }
}

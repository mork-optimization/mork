package es.urjc.etsii.grafo.restcontroller;

import es.urjc.etsii.grafo.solver.services.events.MemoryEventStorage;
import es.urjc.etsii.grafo.solver.services.events.types.MorkEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EventController {

    private final MemoryEventStorage memoryEventStorage;

    public EventController(MemoryEventStorage memoryEventStorage) {
        this.memoryEventStorage = memoryEventStorage;
    }

    @GetMapping("/events")
    public List<MorkEvent> getEvents(@RequestParam int from, @RequestParam int to){
        return memoryEventStorage.getEvents(from, to);
    }
}

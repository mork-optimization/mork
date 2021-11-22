package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.serializers.ResultsSerializer;
import es.urjc.etsii.grafo.solver.services.events.MemoryEventStorage;
import es.urjc.etsii.grafo.solver.services.events.types.ExperimentEndedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * <p>SaveResultsListener class.</p>
 *
 */
@Service
public class SaveResultsListener {

    private final static Logger log = Logger.getLogger(SaveResultsListener.class.getName());
    private final List<ResultsSerializer> resultsSerializers;
    private final MemoryEventStorage eventStorage;

    /**
     * <p>Constructor for SaveResultsListener.</p>
     *
     * @param resultsSerializers a {@link java.util.List} object.
     * @param eventStorage a {@link es.urjc.etsii.grafo.solver.services.events.MemoryEventStorage} object.
     */
    public SaveResultsListener(List<ResultsSerializer> resultsSerializers, MemoryEventStorage eventStorage) {
        this.resultsSerializers = resultsSerializers;
        this.eventStorage = eventStorage;
    }

    /**
     * <p>saveResults.</p>
     *
     * @param event a {@link es.urjc.etsii.grafo.solver.services.events.types.ExperimentEndedEvent} object.
     */
    @EventListener
    @Async
    public void saveResults(ExperimentEndedEvent event){
        String expName = event.getExperimentName();
        var experimentData = eventStorage.getGeneratedSolEventForExp(expName).collect(Collectors.toList());
        int count = 0;
        for (var e : experimentData) {
            if(e.getSolution().isPresent()){
                count++;
            }
        }
        log.info(String.format("Solutions in memory at finish: %s of %s", count, experimentData.size()));
        for (ResultsSerializer resultsSerializer : resultsSerializers) {
            resultsSerializer.serializeResults(expName, experimentData);
        }
    }
}

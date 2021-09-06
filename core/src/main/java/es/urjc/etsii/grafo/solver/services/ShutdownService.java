package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.solver.services.events.EventAsyncConfigurer;
import es.urjc.etsii.grafo.solver.services.events.MorkEventListener;
import es.urjc.etsii.grafo.solver.services.events.types.ExecutionEndedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class ShutdownService {
    private static final Logger log = Logger.getLogger(ShutdownService.class.getName());

    private final ConfigurableApplicationContext appContext;
    private final EventAsyncConfigurer eventAsyncConfigurer;
    private final boolean stopOnExperimentEnd;

    public ShutdownService(ApplicationContext appContext, EventAsyncConfigurer eventAsyncConfigurer, @Value("${event.webserver.stopOnExecutionEnd:true}") boolean stopOnExperimentEnd) {
        this.appContext = (ConfigurableApplicationContext) appContext;
        this.eventAsyncConfigurer = eventAsyncConfigurer;
        this.stopOnExperimentEnd = stopOnExperimentEnd;
    }

    @MorkEventListener
    public void onExperimentationEnd(ExecutionEndedEvent event){
        if(stopOnExperimentEnd){
            log.info("StopOnExperimentEnd enabled, delayed stop executed.");
            delayedStop(5, TimeUnit.SECONDS);
        } else {
            log.info("StopOnExperimentEnd disabled, app must be stopped by user");
        }
    }

    public void delayedStop(long time, TimeUnit unit){
        new Thread(() ->{
            try {
                Thread.sleep(unit.toMillis(time));
                log.info("Shutting down Async Executor");
                eventAsyncConfigurer.shutdownAsyncExecutor();
                log.info("Closing context and exiting");
                appContext.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warning(e.toString());
            }
        }).start();
    }
}

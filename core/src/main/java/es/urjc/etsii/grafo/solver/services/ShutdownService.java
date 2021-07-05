package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.solver.services.events.EventAsyncConfigurer;
import es.urjc.etsii.grafo.solver.services.events.types.ExecutionEndedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class ShutdownService {
    private static final Logger log = Logger.getLogger(ShutdownService.class.getName());

    private final ConfigurableApplicationContext appContext;
    private final EventAsyncConfigurer eventAsyncConfigurer;

    public ShutdownService(ApplicationContext appContext, EventAsyncConfigurer eventAsyncConfigurer) {
        this.appContext = (ConfigurableApplicationContext) appContext;
        this.eventAsyncConfigurer = eventAsyncConfigurer;
    }

    @EventListener
    public void onExperimentationEnd(ExecutionEndedEvent event){
        new Thread(() ->{
            try {
                Thread.sleep(5000);
                log.info("Shutting down Async Executor");
                eventAsyncConfigurer.shutdownAsyncExecutor();
                log.info("Closing context and exiting");
                appContext.close();

            } catch (InterruptedException e) {
                log.warning(e.toString());
            }
        }).start();

    }

}

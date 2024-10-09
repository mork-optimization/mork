package es.urjc.etsii.grafo.services;

import es.urjc.etsii.grafo.events.EventAsyncConfigurer;
import es.urjc.etsii.grafo.events.EventWebserverConfig;
import es.urjc.etsii.grafo.events.MorkEventListener;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * <p>ShutdownService class.</p>
 *
 */
@Service
public class ShutdownService {
    private static final Logger log = LoggerFactory.getLogger(ShutdownService.class);

    private final ConfigurableApplicationContext appContext;
    private final EventAsyncConfigurer eventAsyncConfigurer;
    private final boolean stopOnExperimentEnd;

    /**
     * <p>Constructor for ShutdownService.</p>
     *
     * @param appContext a {@link org.springframework.context.ApplicationContext} object.
     * @param eventAsyncConfigurer a {@link EventAsyncConfigurer} object.
     * @param eventWebserverConfig a {@link EventWebserverConfig} object.
     */
    public ShutdownService(ApplicationContext appContext, EventAsyncConfigurer eventAsyncConfigurer, EventWebserverConfig eventWebserverConfig) {
        this.appContext = (ConfigurableApplicationContext) appContext;
        this.eventAsyncConfigurer = eventAsyncConfigurer;
        this.stopOnExperimentEnd = eventWebserverConfig.isStopOnExecutionEnd();
    }

    /**
     * <p>onExperimentationEnd.</p>
     *
     * @param event a {@link ExecutionEndedEvent} object.
     */
    @MorkEventListener
    public void onExperimentationEnd(ExecutionEndedEvent event){
        if(stopOnExperimentEnd){
            log.info("event.webserver.StopOnExperimentEnd enabled, requesting context close and exiting.");
            delayedStop(1, TimeUnit.SECONDS);
        } else {
            log.info("event.webserver.StopOnExperimentEnd disabled, app must be manually stopped by user");
        }
    }

    /**
     * <p>delayedStop.</p>
     *
     * @param time a long.
     * @param unit a {@link java.util.concurrent.TimeUnit} object.
     */
    public void delayedStop(long time, TimeUnit unit){
        new Thread(() ->{
            try {
                Thread.sleep(unit.toMillis(time));
                log.debug("Shutting down Async Executor");
                eventAsyncConfigurer.shutdownAsyncExecutor();
                log.debug("Closing context and exiting");
                appContext.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn(e.toString());
            }
        }).start();
    }
}

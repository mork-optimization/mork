package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.types.MorkEvent;
import es.urjc.etsii.grafo.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

/**
 * Synchronous Spring event multicaster that isolates failures between Mork
 * listeners while retaining Spring's normal exception behavior for all other
 * application events.
 */
final class MorkApplicationEventMulticaster extends SimpleApplicationEventMulticaster {

    private static final Logger log = LoggerFactory.getLogger(MorkApplicationEventMulticaster.class);

    @Override
    protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
        if (!isMorkEvent(event)) {
            super.invokeListener(listener, event);
            return;
        }

        try {
            super.invokeListener(listener, event);
        } catch (Throwable failure) {
            var rootCause = ExceptionUtil.getRootCause(failure);
            log.error(
                    "Mork event listener {} failed: {}: {}",
                    listener.getClass().getName(),
                    rootCause.getClass().getSimpleName(),
                    rootCause.getMessage(),
                    failure
            );
        }
    }

    private boolean isMorkEvent(ApplicationEvent event) {
        return event instanceof MorkEvent
                || event instanceof PayloadApplicationEvent<?> payloadEvent
                && payloadEvent.getPayload() instanceof MorkEvent;
    }
}

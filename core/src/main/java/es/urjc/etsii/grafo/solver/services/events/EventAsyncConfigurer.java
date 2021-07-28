package es.urjc.etsii.grafo.solver.services.events;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Component
public class EventAsyncConfigurer implements AsyncConfigurer {

    private static final Logger log = Logger.getLogger(EventAsyncConfigurer.class.getName());
    private final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor();

    @Override
    public Executor getAsyncExecutor() {
        return asyncExecutor;
    }

    public void shutdownAsyncExecutor(){
        this.asyncExecutor.shutdown();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.severe(String.format("Async listener FAILED (%s): Exception with message: %s, params: %s, exception: %s", method, ex.getMessage(), Arrays.toString(params), ex));
    }
}

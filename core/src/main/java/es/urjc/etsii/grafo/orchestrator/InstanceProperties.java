package es.urjc.etsii.grafo.orchestrator;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.ExecutionStartedEvent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.monitoring.MonitorModeState.Mode;
import es.urjc.etsii.grafo.services.ExecutionLifecycleCoordinator;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.InstanceSelectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;

import static es.urjc.etsii.grafo.events.types.ExecutionEndedEvent.Outcome.*;

public class InstanceProperties<I extends Instance> extends AbstractOrchestrator {

    @Override
    public Mode getMode() {
        return Mode.UTILITY;
    }

    private static final Logger log = LoggerFactory.getLogger(InstanceProperties.class);
    public static final String DEFAULT_OUTPUT_PATH = "instance_properties.csv";
    public static final Set<String> ignoredProperties = Set.of(Instance.LOAD_TIME_NANOS);
    protected final InstanceManager<I> instanceManager;
    protected final InstanceConfiguration instanceConfiguration;
    protected final MorkEventPublisher eventPublisher;
    protected final ExecutionLifecycleCoordinator lifecycleCoordinator;

    public InstanceProperties(
            InstanceManager<I> instanceManager,
            InstanceConfiguration instanceConfiguration,
            MorkEventPublisher eventPublisher,
            ExecutionLifecycleCoordinator lifecycleCoordinator
    ) {
        this.instanceManager = instanceManager;
        this.instanceConfiguration = instanceConfiguration;
        this.eventPublisher = eventPublisher;
        this.lifecycleCoordinator = lifecycleCoordinator;
    }

    @Override
    public void run(String... args) {
        long start = System.nanoTime();
        var outcome = SUCCEEDED;
        try {
            eventPublisher.publish(new ExecutionStartedEvent(Context.getObjectivesW(), List.of("Instance analysis")));
            analyzeInstances();
        } catch (RuntimeException | Error e) {
            outcome = FAILED;
            throw e;
        } finally {
            long end = System.nanoTime();
            lifecycleCoordinator.complete(end - start, outcome);
        }
    }

    public void analyzeInstances(){
        String path = instanceConfiguration.getForSelection();
        if (path != null && !path.isBlank()) {
            instanceConfiguration.getPaths().put("default", path);
        }
        var instancePaths = this.instanceManager.getInstanceSolveOrder("default", false);
        log.info("Analyzing instances...");
        InstanceSelectionUtil.writeProperties(Path.of(DEFAULT_OUTPUT_PATH), instancePaths, instanceManager);
        log.info("Instance property CSV written");
    }
}

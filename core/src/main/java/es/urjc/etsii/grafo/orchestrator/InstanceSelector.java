package es.urjc.etsii.grafo.orchestrator;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.events.EventPublisher;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.events.types.ExecutionStartedEvent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.IOUtil;
import es.urjc.etsii.grafo.util.PythonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class InstanceSelector<I extends Instance> extends InstanceProperties<I> {

    private static final Logger log = LoggerFactory.getLogger(InstanceSelector.class);

    public InstanceSelector(
            InstanceManager<I> instanceManager,
            InstanceConfiguration instanceConfiguration
    ) {
        super(instanceManager, instanceConfiguration);
    }

    @Override
    public void run(String... args) {
        long start = System.nanoTime();
        try {
            EventPublisher.getInstance().publishEvent(new ExecutionStartedEvent(Context.getObjectivesW(), List.of("Instance analysis")));
            analyzeInstances();
            log.info("Launching selector");
            selectInstances();
            log.info("Completed");
        } finally {
            long end = System.nanoTime();
            EventPublisher.getInstance().publishEvent(new ExecutionEndedEvent(end - start));
        }
    }

    protected void selectInstances() {
        var referenceClass = instanceManager.getUserImporterImplementation().getClass();
        var isJAR = IOUtil.isJAR(referenceClass);

        String size = String.valueOf(instanceConfiguration.getPreliminarPercentage());
        String preOut = instanceConfiguration.getPreliminarOutputPath();
        IOUtil.extractResource("instance-selector/instance-selector.py", "instance_selector.py", isJAR, true);
        IOUtil.extractResource("instance-selector/requirements.txt", "requirements.txt", isJAR, true);

        PythonUtil.run(true, "-m", "pip", "install", "-r", "requirements.txt");
        PythonUtil.run(true, "instance_selector.py", "-o", preOut, "-p", DEFAULT_OUTPUT_PATH, "-s", size);
    }
}

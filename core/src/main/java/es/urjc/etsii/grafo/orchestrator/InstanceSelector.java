package es.urjc.etsii.grafo.orchestrator;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.events.EventPublisher;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.events.types.ExecutionStartedEvent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
            EventPublisher.getInstance().publishEvent(new ExecutionStartedEvent(Context.getObjectives(), List.of("Instance analysis")));
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
        String path = instanceConfiguration.getForSelection();
        if (path == null || path.isBlank()) {
            path = instanceConfiguration.getPath("default");
        }
        var referenceClass = instanceManager.getUserImporterImplementation().getClass();
        var isJAR = IOUtil.isJAR(referenceClass);
        var pb = new ProcessBuilder()
                .inheritIO();
        String size = String.valueOf(instanceConfiguration.getPreliminarPercentage());
        String preOut = instanceConfiguration.getPreliminarOutputPath();
        try {
            IOUtil.extractResource("instance-selector/instance-selector.py", "instance_selector.py", isJAR, true);
            IOUtil.extractResource("instance-selector/requirements.txt", "requirements.txt", isJAR, true);
            // Check if venv exists, if not, create it
            if(!Files.exists(Path.of("venv"))){
                log.info("Creating virtual environment for instance selector");
                pb.command("python3", "-m", "venv", "venv").start().waitFor();
            } else {
                log.info("Virtual environment already exists, skipping creation");
            }

            pb.command("./venv/bin/python3", "-m", "pip", "install", "-r", "requirements.txt").start().waitFor();
            pb.command("./venv/bin/python3", "instance_selector.py", "-i", path, "-o", preOut, "-p", DEFAULT_OUTPUT_PATH, "-s", size).start().waitFor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}

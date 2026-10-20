package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.util.ExecutionJournalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Registered by SpringApplication before bean creation, including failures that prevent orchestrator startup. */
public final class AutoLlmStartupFailureListener implements ApplicationListener<ApplicationFailedEvent> {
    private static final Logger log = LoggerFactory.getLogger(AutoLlmStartupFailureListener.class);

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        if (!ExecutionJournalUtil.enabled()) return;
        Path directory = ExecutionJournalUtil.directory();
        try {
            if (!Files.exists(directory.resolve("failure.json"))) {
                Set<String> baseline = null;
                if (event.getApplicationContext() != null) {
                    String file = event.getApplicationContext().getEnvironment().getProperty("autollm.child.baseline-components-file");
                    if (file != null) {
                        try { baseline = new HashSet<>(List.of(AutoLlmFileUtil.read(Path.of(file), String[].class))); }
                        catch (RuntimeException e) { log.warn("Cannot read baseline inventory; startup failure remains unattributed", e); }
                    }
                }
                ExecutionJournalUtil.write("failure.json", AutoLlmFailureUtil.describe(event.getException(), directory, baseline));
            }
        } catch (RuntimeException e) {
            log.error("Cannot persist startup failure", e);
        }
        try {
            if (!Files.exists(directory.resolve("terminal.json"))) ExecutionJournalUtil.terminal("FAILED");
        } catch (RuntimeException e) {
            log.error("Cannot persist failed startup outcome", e);
        }
    }
}

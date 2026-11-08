package es.urjc.etsii.grafo.autoconfig.autollm;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

/** Native CLI protocol boundary; campaign and process lifecycle remain shared. */
interface GenerationBackend {
    record Launch(List<String> command, Map<String,String> environment, Set<String> removeEnvironment) {}
    Launch prepare(AutoLlmConfig config, Path checkout, Path output, String prompt, String sessionId);
    void refresh(Path output);
    GenerationEvidence.Evidence archive(Path checkout, Path output, String sessionId);
    String completed(Path output, String sessionId, AutoLlmProcessRunner.Result result, Instant startedAt);
}

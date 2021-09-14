package es.urjc.etsii.grafo.solver.irace;

import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class IraceIntegration {

    private final RLangRunner runner;

    public IraceIntegration(RLangRunner runner) {
        this.runner = runner;
    }

    public void runIrace(){
        try {
            File scriptFile = ResourceUtils.getFile("classpath:irace/runner.R");
            String script = Files.readString(scriptFile.toPath(), StandardCharsets.US_ASCII);
            runner.execute(script);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load R runner", e);
        }
    }
}

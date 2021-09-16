package es.urjc.etsii.grafo.solver.irace.runners;

import es.urjc.etsii.grafo.util.IOUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

@Service
@ConditionalOnExpression("${irace.shell}")
public class ShellRLangRunner implements RLangRunner {

    public void execute(InputStream rCode){
        try {
            // No substitutions for this file, only extract
            IOUtil.copyWithSubstitutions(rCode, Path.of("runner.R"), Map.of());

            ProcessBuilder pb = new ProcessBuilder("Rscript", "runner.R");
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            Process p = pb.start();
            p.waitFor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}

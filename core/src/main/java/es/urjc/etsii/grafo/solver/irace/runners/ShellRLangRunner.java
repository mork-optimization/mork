package es.urjc.etsii.grafo.solver.irace.runners;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@ConditionalOnExpression("${irace.shell}")
public class ShellRLangRunner implements RLangRunner {

    public void execute(String rCode){
        try {
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

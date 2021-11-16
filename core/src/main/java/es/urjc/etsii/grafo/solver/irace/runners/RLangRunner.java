package es.urjc.etsii.grafo.solver.irace.runners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class RLangRunner {
    private static final Logger log = Logger.getLogger(RLangRunner.class.getName());

    protected void drainStream(Level level, InputStream stream) throws IOException {
        try (var br = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while((line = br.readLine()) != null){
                log.log(level, line);
            }
        }
    }
    public abstract void execute(InputStream rCode);
}

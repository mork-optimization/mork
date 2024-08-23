package es.urjc.etsii.grafo.autoconfig.r;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs arbitrary R code
 */
public abstract class RLangRunner {
    private static final Logger log = Logger.getLogger(RLangRunner.class.getName());
    private static final String R_ERROR_MARK = "Error";

    /**
     * Drain a given stream to logs
     *
     * @param level LogLevel
     * @param stream stream to drain
     * @throws java.io.IOException if something goes wrong
     */
    protected boolean drainStream(Level level, InputStream stream) throws IOException {
        boolean errored = false;
        try (var br = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while((line = br.readLine()) != null){
                if(line.contains(R_ERROR_MARK)){
                    errored = true;
                }
                log.log(level, line);
            }
        }
        return errored;
    }

    /**
     * Execute the given R code
     *
     * @param rCode R code to execute
     */
    public abstract void execute(InputStream rCode);
}

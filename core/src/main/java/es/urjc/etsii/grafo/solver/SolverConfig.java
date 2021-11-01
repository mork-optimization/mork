package es.urjc.etsii.grafo.solver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Logger;

@Configuration
@ConfigurationProperties(prefix = "solver")
public class SolverConfig {
    private static final Logger log = Logger.getLogger(SolverConfig.class.getName());

    private int seed;
    private String experiments;
    private boolean maximizing;
    private int repetitions;
    private boolean parallelExecutor;
    private int nWorkers;
    private boolean benchmark;

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public String getExperiments() {
        return experiments;
    }

    public void setExperiments(String experiments) {
        this.experiments = experiments;
    }

    public boolean isMaximizing() {
        return maximizing;
    }

    public void setMaximizing(boolean maximizing) {
        this.maximizing = maximizing;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public boolean isParallelExecutor() {
        return parallelExecutor;
    }

    public void setParallelExecutor(boolean parallelExecutor) {
        this.parallelExecutor = parallelExecutor;
    }

    public int getnWorkers() {
        return nWorkers;
    }

    public void setnWorkers(int nWorkers) {
        this.nWorkers = nWorkers;
    }

    public boolean isBenchmark() {
        return benchmark;
    }

    public void setBenchmark(boolean benchmark) {
        this.benchmark = benchmark;
    }
}

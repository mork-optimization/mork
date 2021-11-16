package es.urjc.etsii.grafo.solver;

import es.urjc.etsii.grafo.util.random.RandomType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Logger;

/**
 * Configuration file based on application.yml file.
 * {@see application.yml}
 */
@Configuration
@ConfigurationProperties(prefix = "solver")
public class SolverConfig {
    private static final Logger log = Logger.getLogger(SolverConfig.class.getName());

    /**
     * Global random seed to ensure reproducibility
     */
    private int seed = 1234;

    /**
     * Random generator to use
     */
    private RandomType randomType;

    /**
     * Experiment names
     */
    private String experiments;

    /**
     * Maximize or minimize objective function. True if Maximizing, False if Minimizing
     */
    private boolean maximizing;

    /**
     * How many times should each experiment be repeated.
     */
    private int repetitions = 1;

    /**
     *
     */
    private boolean parallelExecutor = false;

    /**
     * Number of workers to use if parallelExecutor is enabled
     */
    private int nWorkers = -1;

    /**
     * Execute benchmark before starting solver
     */
    private boolean benchmark = false;

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

    public RandomType getRandomType() {
        return randomType;
    }

    public void setRandomType(RandomType randomType) {
        this.randomType = randomType;
    }
}

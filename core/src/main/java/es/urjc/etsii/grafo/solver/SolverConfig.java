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

    /**
     * <p>Getter for the field <code>seed</code>.</p>
     *
     * @return a int.
     */
    public int getSeed() {
        return seed;
    }

    /**
     * <p>Setter for the field <code>seed</code>.</p>
     *
     * @param seed a int.
     */
    public void setSeed(int seed) {
        this.seed = seed;
    }

    /**
     * <p>Getter for the field <code>experiments</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getExperiments() {
        return experiments;
    }

    /**
     * <p>Setter for the field <code>experiments</code>.</p>
     *
     * @param experiments a {@link java.lang.String} object.
     */
    public void setExperiments(String experiments) {
        this.experiments = experiments;
    }

    /**
     * <p>isMaximizing.</p>
     *
     * @return a boolean.
     */
    public boolean isMaximizing() {
        return maximizing;
    }

    /**
     * <p>Setter for the field <code>maximizing</code>.</p>
     *
     * @param maximizing a boolean.
     */
    public void setMaximizing(boolean maximizing) {
        this.maximizing = maximizing;
    }

    /**
     * <p>Getter for the field <code>repetitions</code>.</p>
     *
     * @return a int.
     */
    public int getRepetitions() {
        return repetitions;
    }

    /**
     * <p>Setter for the field <code>repetitions</code>.</p>
     *
     * @param repetitions a int.
     */
    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    /**
     * <p>isParallelExecutor.</p>
     *
     * @return a boolean.
     */
    public boolean isParallelExecutor() {
        return parallelExecutor;
    }

    /**
     * <p>Setter for the field <code>parallelExecutor</code>.</p>
     *
     * @param parallelExecutor a boolean.
     */
    public void setParallelExecutor(boolean parallelExecutor) {
        this.parallelExecutor = parallelExecutor;
    }

    /**
     * <p>Getter for the field <code>nWorkers</code>.</p>
     *
     * @return a int.
     */
    public int getnWorkers() {
        return nWorkers;
    }

    /**
     * <p>Setter for the field <code>nWorkers</code>.</p>
     *
     * @param nWorkers a int.
     */
    public void setnWorkers(int nWorkers) {
        this.nWorkers = nWorkers;
    }

    /**
     * <p>isBenchmark.</p>
     *
     * @return a boolean.
     */
    public boolean isBenchmark() {
        return benchmark;
    }

    /**
     * <p>Setter for the field <code>benchmark</code>.</p>
     *
     * @param benchmark a boolean.
     */
    public void setBenchmark(boolean benchmark) {
        this.benchmark = benchmark;
    }

    /**
     * <p>Getter for the field <code>randomType</code>.</p>
     *
     * @return a {@link es.urjc.etsii.grafo.util.random.RandomType} object.
     */
    public RandomType getRandomType() {
        return randomType;
    }

    /**
     * <p>Setter for the field <code>randomType</code>.</p>
     *
     * @param randomType a {@link es.urjc.etsii.grafo.util.random.RandomType} object.
     */
    public void setRandomType(RandomType randomType) {
        this.randomType = randomType;
    }
}

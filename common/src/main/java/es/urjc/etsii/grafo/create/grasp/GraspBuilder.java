package es.urjc.etsii.grafo.create.grasp;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.Objects;

import static es.urjc.etsii.grafo.util.DoubleComparator.*;

/**
 * Creates GRASP constructive instances using with different configurations, allowing to easily configure them and reuse the configurations.
 *
 * @param <M> Move class
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class GraspBuilder<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> {
    private Boolean greedyRandom;
    private Objective<M,S,I> objective = Context.getMainObjective();
    private AlphaProvider alphaProvider;
    private String alphaType;
    private GRASPListManager<M, S, I> candidateListManager;

    public GraspBuilder() {}

    /**
     * Use greedy random strategy, or in other words, pick a subset of elements whose greedy function value is near the best value and then pick a random element from this subset.
     *
     * @return same builder with its config changed
     */
    public GraspBuilder<M, S, I> withStrategyGreedyRandom() {
        this.greedyRandom = true;
        return this;
    }

    /**
     * Use random greedy strategy, or in other words, pick a subset of elements randomly, and then pick the best element from this subset.
     *
     * @return same builder with its config changed
     */
    public GraspBuilder<M, S, I> withStrategyRandomGreedy() {
        this.greedyRandom = false;
        return this;
    }

    /**
     * Determines both which greedy function is used to evaluate moves, and if the greedy function should be
     * minimized or maximized.
     *
     * @param objective objective function to use
     * @return same builder with its config changed
     */
    public GraspBuilder<M, S, I> withObjective(Objective<M,S,I> objective) {
        this.objective = objective;
        return this;
    }

    /**
     * Configure alpha value to a fixed value. The alpha value is used to determine the randomness / greediness of the method.
     *
     * @param alpha fixed alpha value to use, must be in range [0, 1]. A value of 0 means completely greedy, while a value of one means completely random.
     * @return same builder with its config changed
     */
    public GraspBuilder<M, S, I> withAlphaValue(double alpha) {
        if (isNegative(alpha) || isGreater(alpha, 1)) {
            throw new IllegalArgumentException("Alpha value must be in range [0,1]");
        }
        this.alphaType = String.format("FIXED{a=%.2f}", alpha);
        this.alphaProvider = () -> alpha;
        return this;
    }

    /**
     * Use a random alpha value in each constructive iteration.
     *
     * @return same builder with its config changed
     */
    public GraspBuilder<M, S, I> withAlphaRandom() {
        return this.withAlphaInRange(0, 1);
    }

    /**
     * Pick a random alpha value in range [min, max].
     *
     * @param alphaMin minimum value, inclusive
     * @param alphaMax maximum value, inclusive
     * @return same builder with its config changed
     */
    public GraspBuilder<M, S, I> withAlphaInRange(double alphaMin, double alphaMax) {
        if (!isLess(alphaMin, alphaMax)) {
            throw new IllegalArgumentException("when using an alpha range min must be strictly than max");
        }
        if (isNegative(alphaMin) || isNegative(alphaMax) || isGreater(alphaMin, 1) || isGreater(alphaMax, 1)) {
            throw new IllegalArgumentException("Both min and max alpha values must be in range [0,1]");
        }
        this.alphaType = String.format("RANGE{min=%.2f, max=%.2f}", alphaMin, alphaMax);
        this.alphaProvider = () -> RandomManager.getRandom().nextDouble() * (alphaMax - alphaMin) + alphaMin;
        return this;
    }

    /**
     * Configure any custom strategy for configuring alpha values.
     * The function provided as a first argument will be called once per constructive iteration.
     *
     * @param provider  function that generates an alpha value for each grasp iteration. Using a lambda expression is recommended.
     * @param explained String that explains how the alpha provider is generating the values. Examples: FIXED{a=0.25}, RANGE{min=0.1, max=0.3}
     * @return same builder with its config changed
     */
    public GraspBuilder<M, S, I> withAlphaProvider(AlphaProvider provider, String explained) {
        this.alphaProvider = provider;
        return this;
    }

    /**
     * Configure the GRASP list manager, responsible for generating the initial candidate list and updating it after each move iteration.
     *
     * @param graspListManager GRASP list manager instance
     * @return same builder with its config changed
     */
    public GraspBuilder<M, S, I> withListManager(GRASPListManager<M, S, I> graspListManager) {
        this.candidateListManager = Objects.requireNonNull(graspListManager);
        return this;
    }

    /**
     * Build the GRASP constructive with the config specified previously. Before building the constructive method,
     * a set of checks are executed in order to verify that al parameters are assigned and have valid values.
     *
     * @return an instance of a GRASP constructive
     * @throws IllegalArgumentException if any config value is not valid or there are missing values
     */
    public GRASPConstructive<M, S, I> build() {
        validate();
        if (this.greedyRandom) {
            return new GreedyRandomGRASPConstructive<>(this.objective, this.candidateListManager, this.alphaProvider, this.alphaType);
        } else {
            return new RandomGreedyGRASPConstructive<>(this.objective, this.candidateListManager,  this.alphaProvider, this.alphaType);
        }
    }

    private void validate() {
        if (this.objective == null) {
            throw new IllegalArgumentException("objective not configured, call GraspBuilder::withObjective");
        }

        if (this.greedyRandom == null) {
            throw new IllegalArgumentException("greedyRandom parameter not configured, call GraspBuilder::withStrategyGreedyRandom or GraspBuilder::withStrategyRandomGreedy to configure either option");
        }

        if (this.alphaType == null || this.alphaProvider == null) {
            throw new IllegalArgumentException("alpha not configured, call any GraspBuilder::withAlpha* method to set either a fixed value, random...");
        }
    }
}
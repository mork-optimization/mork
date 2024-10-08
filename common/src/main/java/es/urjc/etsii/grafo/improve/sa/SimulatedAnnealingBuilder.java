package es.urjc.etsii.grafo.improve.sa;

import es.urjc.etsii.grafo.improve.sa.cd.CoolDownControl;
import es.urjc.etsii.grafo.improve.sa.cd.ExponentialCoolDown;
import es.urjc.etsii.grafo.improve.sa.initialt.ConstantInitialTemperature;
import es.urjc.etsii.grafo.improve.sa.initialt.InitialTemperatureCalculator;
import es.urjc.etsii.grafo.improve.sa.initialt.MaxDifferenceInitialTemperature;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.DoubleComparator;

/**
 * <p>Create instances of the simulated annealing algorithm. See {@link SimulatedAnnealing} for a detailed description of the algorithm</p>
 * @param <M> Move type
 * @param <S> Solution type
 * @param <I> Instance type
 */
public class SimulatedAnnealingBuilder<M extends Move<S, I>, S extends Solution<S,I>, I extends Instance> {
    private RandomizableNeighborhood<M, S, I> neighborhood;
    private AcceptanceCriteria<M, S, I> acceptanceCriteria;
    private InitialTemperatureCalculator<M, S, I> initialTemperatureCalculator;
    private TerminationCriteria<M, S, I> terminationCriteria;
    private CoolDownControl<M, S, I> coolDownControl;
    private int cycleLength = 1;
    private Objective<M,S,I> objective;

    /**
     * Neighborhood for the Simulated Annealing.
     * Tip: You may use a neighborhood composed or several others, see {@link Neighborhood#concat(Neighborhood[])} and {@link Neighborhood#interleave(Neighborhood[])}.
     *
     * @param neighborhood neighborhood
     * @return simulated annealing builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withNeighborhood(RandomizableNeighborhood<M, S, I> neighborhood) {
        this.neighborhood = neighborhood;
        return this;
    }

    /**
     * Provide a custom method for calculating the initial temperature.
     * Example: {@code (solution, neighborhood) -> solution.getNVertex() * 100}
     *
     * @param initialTemperatureCalculator lambda expression or class implementation
     * @return simulated annealing builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withInitialTempFunction(InitialTemperatureCalculator<M, S, I> initialTemperatureCalculator) {
        this.initialTemperatureCalculator = initialTemperatureCalculator;
        return this;
    }

    /**
     * Provide a constant initial temperature
     *
     * @param initialTemp fixed initial temperature
     * @return simulated annealing builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withInitialTempValue(double initialTemp) {
        this.initialTemperatureCalculator = new ConstantInitialTemperature<>(initialTemp);
        return this;
    }

    /**
     * Calculate initial temp as the difference between the best and worst moves in the given neighborhood
     *
     * @return simulated annealing builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withInitialTempMaxValue(Objective<M,S,I> objective) {
        this.initialTemperatureCalculator = new MaxDifferenceInitialTemperature<>(objective);
        return this;
    }

    /**
     * Calculate initial temp as the difference between the best and worst moves in the given neighborhood
     *
     * @param ratio Multiply max difference by this parameter
     * @return simulated annealing builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withInitialTempMaxValue(Objective<M,S,I> objective, double ratio) {
        this.initialTemperatureCalculator = new MaxDifferenceInitialTemperature<>(objective, ratio);
        return this;
    }

    /**
     * Set a custom termination criteria. SA always ends when there is no move that improves and no movement is accepted
     * Can end sooner if the given method decides to end.
     * Example 1: {@code (solution, neighborhood, currentTemp, currentIter) -> currentIter > 100}
     * Example 2: {@code (solution, neighborhood, currentTemp, currentIter) -> currentTemp < solution.getNElements()}
     *
     * @param terminationCriteria custom termination criteria
     * @return simulated annealing builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withTerminationCriteriaCustom(TerminationCriteria<M, S, I> terminationCriteria) {
        this.terminationCriteria = terminationCriteria;
        return this;
    }

    /**
     * End when the maximum number of iterations is reached.
     * Can end sooner if we cannot apply any move
     *
     * @return builder
     * @param n a int.
     */
    public SimulatedAnnealingBuilder<M,S,I> withTerminationCriteriaMaxIterations(int n) {
        this.terminationCriteria = ((sol, neighborhood, currentTemp, iteration) -> iteration >= n);
        return this;
    }

    /**
     * End when temperature reaches 0.
     *
     * @return simulated annealing builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withTerminationCriteriaConverge() {
        this.terminationCriteria = ((sol, neighborhood, currentTemp, iteration) -> DoubleComparator.isLessOrEquals(currentTemp, 0.01));
        return this;
    }


    /**
     * Set a custom cool down function. Consider submitting a PR if it is generally aplicable.
     * Example (halve each iteration): {@code (solution, neighborhood, currentTemp, currentIter) -> currentTemp / 2}
     *
     * @param coolDownControl custom cool down function.
     * @return simulated annealing builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withCoolDownCustom(CoolDownControl<M, S, I> coolDownControl) {
        this.coolDownControl = coolDownControl;
        return this;
    }

    /**
     * Use an exponential cool down function.
     * Example (halve each iteration): {@code (solution, neighborhood, currentTemp, currentIter) -> currentTemp / 2}
     *
     * @param ratio exponential ratio, i.e temp = initialT * (ratio ^ iteration)
     * @return simulated annealing builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withCoolDownExponential(double ratio) {
        this.coolDownControl = new ExponentialCoolDown<>(ratio);
        return this;
    }

    /**
     * Set cycle length
     *
     * @param cycleLength How many moves should be executed for each temperature level.
     *                    Defaults to 1.
     * @return simulated annealing builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withCycleLength(int cycleLength) {
        this.cycleLength = cycleLength;
        return this;
    }

    /**
     * Set a custom score evaluation function for any move
     *
     * @return simulated annealing builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withObjective(Objective<M,S,I> objective) {
        this.objective = objective;
        return this;
    }


    /**
     * Configure a custom acceptance criteria.
     * @param acceptanceCriteria acceptance criteria.
     */
    public SimulatedAnnealingBuilder<M,S,I> withAcceptanceCriteriaCustom(AcceptanceCriteria<M, S, I> acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
        return this;
    }

    /**
     * Set acceptance criteria to default value, based on the metropolis exponential function
     */
    public SimulatedAnnealingBuilder<M,S,I> withAcceptanceCriteriaDefault(Objective<M, S, I> objective){
        this.acceptanceCriteria = new MetropolisAcceptanceCriteria<>(objective);
        return this;
    }

    /**
     * Build a SimulatedAnnealing using the provided config values. Default values if not explicitly set:
     * - Acceptance: Metropolis
     * - Initial temp: max difference between moves in neighborhood
     * - Cooling Schedule: Exponential with ratio 0.99
     * - Termination criteria: temperature convergence
     * - Cycle length: 1
     * - Objective: Context::getMainObjective
     *
     * @return configured and ready to use simulated annealing algorithm
     */
    public SimulatedAnnealing<M,S,I> build() {
        if(this.neighborhood == null){
            throw new IllegalArgumentException("Cannot create simulated annealing without a neighborhood, use withNeighborhood method");
        }

        Objective<M,S,I> objective = this.objective == null? Context.getMainObjective() : this.objective;
        if(objective == null){
            throw new IllegalArgumentException("Cannot create simulated annealing without specifying an objective function. Use withObjective method to configure");
        }

        if(this.initialTemperatureCalculator == null){
            this.withInitialTempMaxValue(objective);
        }

        if(this.coolDownControl == null){
            this.withCoolDownExponential(0.99);
        }

        if(this.terminationCriteria == null){
            this.withTerminationCriteriaConverge();
        }

        if(acceptanceCriteria == null){
            this.withAcceptanceCriteriaDefault(objective);
        }

        if(cycleLength <= 0){
            throw new IllegalArgumentException("Cycle length must be > 0");
        }

        return new SimulatedAnnealing<>(objective, acceptanceCriteria, neighborhood, initialTemperatureCalculator, terminationCriteria, coolDownControl, this.cycleLength);
    }
}

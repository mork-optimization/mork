package es.urjc.etsii.grafo.solver.improve.sa;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.solver.improve.sa.cd.CoolDownControl;
import es.urjc.etsii.grafo.solver.improve.sa.cd.ExponentialCoolDown;
import es.urjc.etsii.grafo.solver.improve.sa.initialt.ConstantInitialTemperature;
import es.urjc.etsii.grafo.solver.improve.sa.initialt.InitialTemperatureCalculator;
import es.urjc.etsii.grafo.solver.improve.sa.initialt.MaxDifferenceInitialTemperature;
import es.urjc.etsii.grafo.util.DoubleComparator;

/**
 * <p>SimulatedAnnealingBuilder class.</p>
 *
 */
public class SimulatedAnnealingBuilder<M extends Move<S, I>, S extends Solution<S,I>, I extends Instance> {
    private Neighborhood<M, S, I> neighborhood;
    private AcceptanceCriteria<M, S, I> acceptanceCriteria;
    private InitialTemperatureCalculator<M, S, I> initialTemperatureCalculator;
    private TerminationCriteria<M, S, I> terminationCriteria;
    private CoolDownControl<M, S, I> coolDownControl;
    private int cycleLength = 1;

    /**
     * Use SimulatedAnnealing::builder static method instead
     */
    protected SimulatedAnnealingBuilder(){}

    /**
     * Neighborhood for the SA
     *
     * @param neighborhood neighborhood
     * @return builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withNeighborhood(Neighborhood<M, S, I> neighborhood) {
        this.neighborhood = neighborhood;
        return this;
    }

    /**
     * Provide a custom method for calculating the initial temperature.
     * Example: {@code (solution, neighborhood) -> solution.getNVertex() * 100}
     *
     * @param initialTemperatureCalculator lambda expression or class implementation
     * @return builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withCustomInitialTempCalc(InitialTemperatureCalculator<M, S, I> initialTemperatureCalculator) {
        this.initialTemperatureCalculator = initialTemperatureCalculator;
        return this;
    }

    /**
     * Provide a constant initial temperature
     *
     * @param initialTemp fixed initial temperature
     * @return builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withInitialTemp(double initialTemp) {
        this.initialTemperatureCalculator = new ConstantInitialTemperature<>(initialTemp);
        return this;
    }

    /**
     * Calculate initial temp as the difference between the best and worst moves in the given neighborhood
     *
     * @return builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withMaxDiffInitialTemp() {
        this.initialTemperatureCalculator = new MaxDifferenceInitialTemperature<>();
        return this;
    }

    /**
     * Calculate initial temp as the difference between the best and worst moves in the given neighborhood
     *
     * @param ratio Multiply max difference by this parameter
     * @return builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withMaxDiffInitialTemp(double ratio) {
        this.initialTemperatureCalculator = new MaxDifferenceInitialTemperature<>(ratio);
        return this;
    }

    /**
     * Set a custom termination criteria. SA always ends when there is no move that improves and no movement is accepted
     * Can end sooner if the given method decides to end.
     * Example 1: {@code (solution, neighborhood, currentTemp, currentIter) -> currentIter > 100}
     * Example 2: {@code (solution, neighborhood, currentTemp, currentIter) -> currentTemp < solution.getNElements()}
     *
     * @param terminationCriteria custom termination criteria
     * @return builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withCustomTerminationCriteria(TerminationCriteria<M, S, I> terminationCriteria) {
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
    public SimulatedAnnealingBuilder<M,S,I> withMaxIterationsTerminationCriteria(int n) {
        this.terminationCriteria = ((sol, neighborhood, currentTemp, iteration) -> iteration >= n);
        return this;
    }

    /**
     * End when temperature reaches 0.
     *
     * @return builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withConvergeTerminationCriteria() {
        this.terminationCriteria = ((sol, neighborhood, currentTemp, iteration) -> DoubleComparator.isLessOrEquals(currentTemp, 0.01));
        return this;
    }


    /**
     * Set a custom cool down function. Consider submitting a PR if it is generally aplicable.
     * Example (halve each iteration): {@code (solution, neighborhood, currentTemp, currentIter) -> currentTemp / 2}
     *
     * @param coolDownControl custom cool down function.
     * @return builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withCustomCoolDownControl(CoolDownControl<M, S, I> coolDownControl) {
        this.coolDownControl = coolDownControl;
        return this;
    }

    /**
     * Use an exponential cool down function.
     * Example (halve each iteration): {@code (solution, neighborhood, currentTemp, currentIter) -> currentTemp / 2}
     *
     * @param ratio exponential ratio, i.e temp = initialT * (ratio ^ iteration)
     * @return builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withExponentialCoolDown(double ratio) {
        this.coolDownControl = new ExponentialCoolDown<>(ratio);
        return this;
    }

    /**
     * Configure cycle length, defaults to 1 if not called.
     *
     * @param cycleLength How many moves should be executed for each temperature level.
     *                    Defaults to 1.
     * @return builder
     */
    public SimulatedAnnealingBuilder<M,S,I> withCycleLength(int cycleLength) {
        this.cycleLength = cycleLength;
        return this;
    }

    /**
     * Build a SimulatedAnnealing using the provided config values. Default values are as follows:
     *
     * @return SimulatedAnnealing algorithm
     */
    public SimulatedAnnealing<M,S,I> build() {
        if(this.neighborhood == null){
            throw new IllegalArgumentException("Cannot create simulated annealing without a neighborhood, use withNeighborhood method");
        }
        if(this.initialTemperatureCalculator == null){
            throw new IllegalArgumentException("Cannot create simulated annealing without a neighborhood, use withInitialTemp to configure");
        }
        if(this.coolDownControl == null){
            throw new IllegalArgumentException("Cannot create simulated annealing without a cool down function, use withCoolDown functions to configure");
        }
        if(this.terminationCriteria == null){
            throw new IllegalArgumentException("Cannot create simulated annealing without a termination criteria, use withTerminationCriteria functions to configure. If you want to execute until the solution converges, use withConvergeTerminationCriteria");
        }
        if(cycleLength <= 0){
            throw new IllegalArgumentException("Cycle length must be > 0");
        }
        if(acceptanceCriteria == null){
            throw new IllegalArgumentException("Acceptance criteria is not configured. Use either withAcceptanceCriteria or withDefaultAcceptanceCriteria to configure an acceptance criteria.");
        }
        return new SimulatedAnnealing<>(acceptanceCriteria, neighborhood, initialTemperatureCalculator, terminationCriteria, coolDownControl, this.cycleLength);
    }

    /**
     * Configure a custom acceptance criteria.
     * @param acceptanceCriteria acceptance criteria.
     */
    public void withAcceptanceCriteria(AcceptanceCriteria<M, S, I> acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
    }

    /**
     * Set acceptance criteria to default value.
     */
    public void withDefaultAcceptanceCriteria(){
        this.acceptanceCriteria = new DefaultAcceptanceCriteria<>();
    }
}

package es.urjc.etsii.grafo.algorithms.vns;

import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.StringUtil;

public class VNSBuilder<S extends Solution<S,I>, I extends Instance> {

    /**
     * Objective to optimize
     * Defaults to the default objective declared in the current Context
     */
    protected Objective<?, S, I> objective;

    /**
     * Neighborhood change strategy
     * Defaults to DefaultVNSNeighChange with maxK = 10, increment = 1
     */
    private VNSNeighChange<S,I> neighChange;

    /**
     * Constructive procedure to use when generating initial solutions.
     * If not set, it will default to the main constructive procedure in the current Context.
     */
    private Constructive<S,I> constructive;

    /**
     * Improvement method
     */
    private Improver<S,I> improver;

    /**
     * Shake procedure to use when perturbing the solution.
     */
    private Shake<S,I> shake;

    /**
     * Creates a new VNSBuilder instance.
     * @param constructive Constructive procedure to use when generating initial solutions.
     * @return This builder instance for method chaining
     */
    public VNSBuilder<S,I> withConstructive(Constructive<S,I> constructive) {
        this.constructive = constructive;
        return this;
    }

    /**
     * Configures the improvement procedure
     * @param improver Improvement procedure to use when improving the solution.
     * @return This builder instance for method chaining
     */
    public VNSBuilder<S,I> withImprover(Improver<S,I> improver) {
        this.improver = improver;
        return this;
    }

    /**
     * Configures the shake procedure.
     * @param shake Shake procedure to use when perturbing the solution.
     * @return This builder instance for method chaining
     */
    public VNSBuilder<S,I> withShake(Shake<S,I> shake) {
        this.shake = shake;
        return this;
    }

    /**
     * Optimize default objective function, which is the main objective declared in the current Context.
     * Note that calling this method is optional, if the objective is not set it will default to the main objective.
     * @return This builder instance for method chaining
     */
    public VNSBuilder<S,I> withDefaultObjective() {
        this.objective = Context.getMainObjective();
        return this;
    }

    /**
     * Configures the objective function to optimize.
     * @param objective Objective function to optimize.
     * @return This builder instance for method chaining
     */
    public VNSBuilder<S,I> withObjective(Objective<?, S, I> objective) {
        this.objective = objective;
        return this;
    }

    /**
     * Configures the neighborhood change strategy.
     * Provide a custom implementation of VNSNeighChange to define how the k value should change.
     * And when the VNS should stop.
     * @param neighChange Custom neighborhood change strategy.
     *                    This should implement the VNSNeighChange interface,
     *                    or be a lambada function that takes a solution and an integer k,
     *                    and returns an integer k value (the updated k value).
     *                    Example: withNeighChange((solution, k) -> k+1);
     *                    // note that in this example k will keep increasing forever if the solution does not improve
     * @return This builder instance for method chaining
     */
    public VNSBuilder<S,I> withNeighChange(VNSNeighChange<S, I> neighChange) {
        this.neighChange = neighChange;
        return this;
    }

    /**
     * Configures the neighborhood change strategy with a maximum K value.
     * Increment defaults to 1.
     * @param maxK Maximum K value for the neighborhood change. When k reaches this value, the VNS will stop.
     * @return This builder instance for method chaining
     */
    public VNSBuilder<S,I> withNeighChange(int maxK) {
        withNeighChange((a,b) -> b+1);
        return this.withNeighChange(maxK, 1);
    }

    /**
     * Configures the neighborhood change strategy with a maximum K value and a one-by-one increment.
     * @param maxK Maximum K value for the neighborhood change. When k reaches this value, the VNS will stop.
     * @param increment Increment to apply to K when the solution does not improve
     * @return This builder instance for method chaining
     */
    public VNSBuilder<S,I> withNeighChange(int maxK, int increment) {
        return this.withNeighChange(new DefaultVNSNeighChange<>(maxK, increment));
    }

    /**
     * Builds the VNS algorithm with the configured parameters.
     * Uses a random name for the algorithm.
     * @return A new instance of VNS
     */
    public VNS<S,I> build() {
        return this.build(StringUtil.randomAlgorithmName());
    }

    /**
     * Builds the VNS algorithm with the configured parameters.
     * @return A new instance of VNS
     */
    public VNS<S,I> build(String name) {
        Objective<?,S,I> objective = this.objective == null? Context.getMainObjective() : this.objective;

        if(this.neighChange == null){
            throw new IllegalArgumentException("VNSBuilder requires a VNSNeighChange implementation. Use .withNeighChange() to configure it.");
        }

        if(this.constructive == null){
            throw new IllegalArgumentException("VNSBuilder requires a Constructive implementation. Use .withConstructive() to configure it.");
        }

        if(this.improver == null){
            throw new IllegalArgumentException("VNSBuilder requires an Improver implementation. Use .withImprover() to configure it.");
        }

        if(this.shake == null){
            throw new IllegalArgumentException("VNSBuilder requires a Shake implementation. Use .withShake() to configure it.");
        }

        return new VNS<>(name, objective, neighChange, constructive, shake, improver);
    }
}

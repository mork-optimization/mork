package es.urjc.etsii.grafo.algorithms.scattersearch;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.StringUtil;

import java.util.Objects;

public class ScatterSearchBuilder<S extends Solution<S,I>, I extends Instance> {
    /**
     * Name of the algorithm
     */
    protected String name = StringUtil.randomAlgorithmName();

    /**
     * When initializing the refset inside the Scatter Search, build (initialRatio * refSetSize) solutions,
     * and choose from them the set of the best solutions and diverse solutions.
     * The bigger the initialRatio, the better the initial solutions and more diverse,
     * but it will take longer to initialize the refset and reset it if it gets stuck.
     */
    protected double initialRatio = 1;

    /**
     * Maximum number of iterations, set by default to a value near Integer.MAX_VALUE
     */
    protected int maxIterations = Integer.MAX_VALUE / 2;

    /**
     * Refset size
     */
    protected int refsetSize = -1;

    /**
     * Proportion of items in refset to select using diversity criteria
     * instead of a greedy or by value one. Must be in range [0, 1]
     */
    protected double diversityRatio = 0;

    /**
     * True if maximizing objective function, false otherwise.
     * Tip: Can be obtained by calling Mork.isMaximizing() or hardcoded in the experiment.
     */
    protected FMode fmode;

    /**
     * Constructive to use to create "good value" solutions
     */
    protected Constructive<S, I> constructiveGoodValues;

    /**
     * Constructive to use when creating "diverse" solutions
     */
    protected Constructive<S, I> constructiveDiverseValues;

    /**
     * Improvement method
     */
    protected Improver<S, I> improver = Improver.nul();

    /**
     * Defines how solutions are combined to create the new candidate refset
     */
    protected SolutionCombinator<S, I> combinator;

    /**
     * How solution distance is measured, useful to calculate diversity metrics
     */
    protected SolutionDistance<S, I> solutionDistance;

    /**
     * Create a new Scatter Search Builder. After configuring the parameters, call ref build()
     */
    public ScatterSearchBuilder() {}

    /**
     * Configure constructive method for initial refset generation.
     * If no diverse constructive method is specified,
     * this constructive method will be used by default to build diverse solutions
     * @param constructive constructive method
     * @return builder
     */
    public ScatterSearchBuilder<S,I> withConstructive(Constructive<S,I> constructive){
        this.constructiveGoodValues = Objects.requireNonNull(constructive);
        if(this.constructiveDiverseValues == null){
            this.constructiveDiverseValues = constructive;
        }
        return this;
    }

    /**
     * Configure a constructive method to use only when diverse solutions are required
     * @param constructive constructive method
     * @return builder
     */
    public ScatterSearchBuilder<S,I> withConstructiveForDiversity(Constructive<S,I> constructive){
        this.constructiveDiverseValues = Objects.requireNonNull(constructive);
        return this;
    }

    /**
     * Configure an improvement method for the Scatter Search algorithm. The improvement method,
     * if provided, will be executed after constructing each solution.
     * @param improver improvement method
     * @return builder
     */
    public ScatterSearchBuilder<S,I> withImprover(Improver<S,I> improver){
        this.improver = Objects.requireNonNull(improver);
        return this;
    }

    /**
     * Configure the method used to generate a new candidate set in each Scatter Search algorithm
     * @param combinator non null combinator method
     * @return builder
     */
    public ScatterSearchBuilder<S,I> withCombinator(SolutionCombinator<S,I> combinator){
        this.combinator = Objects.requireNonNull(combinator);
        return this;
    }

    /**
     * Configure how distance is calculated between different solutions,
     * needed to see how diverse are the solutions.
     * @param distance distance calculation implementation
     * @return builder
     */
    public ScatterSearchBuilder<S,I> withDistance(SolutionDistance<S,I> distance){
        this.solutionDistance = Objects.requireNonNull(distance);
        return this;
    }

    /**
     * Refset size, common values are in range [10,30]
     * @param size number of elements in refset
     * @return builder
     */
    public ScatterSearchBuilder<S,I> withRefsetSize(int size){
        if(size < 0 || size > 1_000_000){
            throw new IllegalArgumentException("Refset size must be in range [1, 999_999]");
        }
        this.refsetSize = size;
        return this;
    }

    /**
     * Configure diversity ratio, ie, number of elements that during refset initialization
     * will be accepted maximizing distance to existing solutions, instead of by objective function value.
     * @param ratio double in range [0, 1]
     * @return builder
     */
    public ScatterSearchBuilder<S,I> withDiversity(double ratio){
        if(ratio < 0 || ratio > 1){
            throw new IllegalArgumentException("Diversity ratio must be in range [0, 1]");
        }
        this.diversityRatio = ratio;
        return this;
    }

    /**
     * Configure algorithm name
     * @param name algorithm name, must not be blank
     * @return builder
     */
    public ScatterSearchBuilder<S,I> withName(String name){
        this.name = Objects.requireNonNull(name).trim();
        if(this.name.isEmpty()){
            throw new IllegalArgumentException("Empty algorithm name");
        }
        return this;
    }

    /**
     * Optional parameter, specifies how many extra solutions to build during refset initialization
     * When initializing the refset inside the Scatter Search, build (initialRatio * refSetSize) solutions,
     * and choose from them the set of the best solutions and diverse solutions.
     * The bigger the initialRatio, the better the initial solutions and more diverse,
     * but it will take longer to initialize the refset and reset it if it gets stuck.
     * @param ratio create ratio*initialRefset solutions to choose from for both by value and by diversity acceptance during initial refset initialization.
     *              If not configured defaults to 1.
     * @return builder
     */
    public ScatterSearchBuilder<S,I> withInitialRatio(double ratio){
        if(ratio < 1){
            throw new IllegalArgumentException("Ratio must be >0");
        }
        this.initialRatio = ratio;
        return this;
    }

    /**
     * Optional parameter, specifies how many scatter search iterations to execute before stopping.
     * Defaults to a huge number, if a TimeLimit is not configured, the algorithm will run indefinitely.
     * @param maxIterations maximum number of iterations. Defaults to Integer.MAX_VALUE / 2 or an equivalent huge number.
     * @return builder
     */
    public ScatterSearchBuilder<S,I> withMaxIterations(int maxIterations){
        if(maxIterations < 1){
            throw new IllegalArgumentException("maxIterations must be >0");
        }
        this.maxIterations = maxIterations;
        return this;
    }

    /**
     * Configure if problem objective function is maximizing or minimizing
     * @param maximizing true if maximizing, false otherwise
     * @return builder
     */
    @Deprecated(forRemoval = true)
    public ScatterSearchBuilder<S,I> withMaximizing(boolean maximizing){
        this.fmode = maximizing == true? FMode.MAXIMIZE: FMode.MINIMIZE;
        return this;
    }

    /**
     * Set solving mode
     * @param fmode MAXIMIZING if maximizing, MINIMIZING if minimizing
     * @return current builder with solving mode set
     */
    public ScatterSearchBuilder<S,I> withSolvingMode(FMode fmode){
        this.fmode = fmode;
        return this;
    }

    /**
     * Build a Scatter Search algorithm with the configured parameters of this builder
     * @return New instance of Scatter Search algorithm. Same builder can generate multiple algorithm instances.
     */
    public ScatterSearch<S,I> build(){
        if(this.constructiveGoodValues == null){
            throw new IllegalArgumentException("no constructive method has been configured");
        }

        if(this.fmode == null){
            throw new IllegalArgumentException("Null maximizing value");
        }
        
        return new ScatterSearch<>(name,
                initialRatio,
                refsetSize,
                constructiveGoodValues,
                constructiveDiverseValues,
                improver,
                combinator,
                fmode,
                maxIterations,
                diversityRatio,
                solutionDistance
        );
    }
}

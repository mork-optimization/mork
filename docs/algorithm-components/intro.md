# Algorithm components

## Definition
An algorithm component is defined as any piece of code that manipulates solutions for an optimization problem in any way. 
Algorithm components are usually implemented in classes, using a Java class per component. Algorithm components are classified in types according to their functionality and responsibility.

## Types

In order to simplify development and clearly identify different component types, Mork classifies all its components using the following types:

Common algorithm components available in the framework are classified in the following roles, depending on the functionality that they contain:

- Algorithm: receives an instance and return a feasible solution. Instead of implementing all the required functionality inside this component type, different steps are delegated to different component types, which are declared as dependencies. For example, most heuristic and metaheuristic methods need a constructive method in order to initialize feasible solutions, before executing the rest of the algorithm steps.
- Constructive: receives an empty solutions and initializes it. Solutions created by constructive methods must be feasible. Commonly used constructive methods, usually easy to implement, are random and greedy approaches. A more elaborate option is using a GRASP based strategy, see [GRASP](todo) for more details about GRASP.
- Improver: receives a solution and tries to improve its objective function score. Improver methods cannot return a solution with worse score that the input solution. A classic example of an improver method are local search methods.
- Shake: also known as perturbation method in the literature. Shake methods try to scape local optima by applying an strategy that may worsen the objective function value of the given solution. Usually used in combination with improver methods. An example strategy is applying random moves to the solution, without checking if the applied moves improve or worsen the objective function value.

All components types, and their implementations, are available in the `common` module.

!!! tip
    You can create as many algorithm component types, ignoring the hierarchy provided by Mork if necessary. See the advanced section at the end.

## Implementing components
This section assumes that you have already created a Mork project. If you have not, please visit the [Getting started](../quickstart/starting.md) section.
In order to create a custom algorithm, you will need to create a new class that extends the `Algorithm` class.
For example, the following code implements an algorithm that creates `n` solutions,
and applies an improver method to the best solution found during the construction phase. 
Note that from the point of view of the algorithm, both the constructive and improver
methods are black boxes whose implementation is unknown. This principle is critical if code reusability is desired,
as we could then execute it and compare the performance of our algorithm for example when using different constructive methods.

!!! info
    The algorithm code contains more advanced features such as logging, time control, and metrics reporting. Although each feature is detailed in their respective page, a small summary is provided in this example.

```java
public class MyAlgorithm<S extends Solution<S, I>, I extends Instance> extends Algorithm<S, I> {
    private static Logger log = LoggerFactory.getLogger(MyAlgorithm.class));

    protected final int n;
    protected final Constructive<S, I> constructive;
    protected final Improver<S, I> improver;

    public MyAlgorithm(String algorithmName, int n, Constructive<S, I> constructive, Improver<S, I> improver) {
        super(algorithmName);
        this.n = n;
        this.constructive = constructive;
        this.improver = improver;
    }

    @Override
    public S algorithm(I instance) {
        var solution = constructionPhase(instance);
        solution = localSearchPhase(solution);
        return solution;
    }

    protected S constructionPhase(I instance){
        var bestSolution = constructionStep(instance);
        Metrics.add(BestObjective.class, bestSolution.getScore());
        for (int i = 1; i < n && !TimeControl.isTimeUp(); i++) {
            var solution = constructionStep(instance);
            if(solution.isBetterThan(bestSolution)){
                log.debug("Improved best solution. {} --> {}", bestSolution, solution);
                bestSolution = solution;
                Metrics.add(BestObjective.class, bestSolution.getScore());
            }
        }
        log.debug("Best Construction: {}", bestSolution);
        return bestSolution;
    }

    protected S constructionStep(I instance){
        var solution = this.newSolution(instance);
        solution = constructive.construct(solution);
        ValidationUtil.assertValidScore(solution);
        log.trace("New solution: {}", solution);
        return solution;
    }

    protected S localSearchPhase(S solution) {
        if (TimeControl.isTimeUp()) {
            return solution;
        }
        solution = improver.improve(solution);
        ValidationUtil.assertValidScore(solution);
        Metrics.add(BestObjective.class, solution.getScore());
        log.debug("After LS: {}", solution);
        return solution;
    }

    @Override
    public String toString() {
        return "MyAlgorithm{" +
                "n=" + n +
                ", c=" + constructive +
                ", i=" + improver +
                '}';
    }
}
```

!!! info
    Algorithms should avoid containing problem specific behaviour, delegating it to the appropriate components.

The process is similar for any component type. See the [TSP example](../examples/TSP.md) for a guided tutorial on solving your first optimization problem using Mork.


## Advanced

One of the core design principles of the Mork framework is its flexibility. In this section, we will explain how to create custom components, and some Mork internals and design decisions.
The only requirement to integrate with the framework, is using the algorithm base class.

### Creating custom types

As a rule of thumb, always use class inheritance to define the component types, and use interfaces to define its capabilities. If possible, extend the most specific existing type.
However, some metaheuristic families define their own types, that may not be relevant in other metaheuristic methods. For example,

### Autodetect components
Any Java class is automatically detected by the Mork engine as an algorithm component if it is either marked with the annotation `@AlgorithmComponent`, or any superclass is annotated with it (does not include interfaces).

!!! tip
    Remember: all algorithm component types provided by Mork are annotated, so you do not need to use the `@AlgorithmComponent` annotation if your components extend `Algorithm`, `Constructive`, `Improver`, etc., or any other component type class.


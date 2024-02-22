# Algorithm components

## Definition
An algorithm component is any piece of code that can modify a solution in any way in the context of an optimization problem. 
Algorithm components are usually implemented in classes, using a Java class per component. 
Algorithm components can be classified according to their functionality and responsibility, or in other words, what is their expected behaviour.

## Algorithm component types

!!! tip
    Mork provides a set of algorithm component types, which are the building blocks of any optimization algorithm. While we recommend using the provided component types, and extending the hierarchy as required, you can create your own component types hierarchy from scratch.

All components types, and the implementations provided in the framework, are available in the `common` module.
All the algorithm components provided by the framework are classified in the following roles, depending on what is expected of them.

### Algorithm

An algorithm is a high level component which implements an strategy to generate solutions for a given problem. It must receive an instance, and return a feasible solution.

Traditionally, algorithms have been classified in two main categories: exact and heuristic. Exact algorithms are guaranteed to find the optimal solution, while heuristic algorithms are not. Heuristic algorithms are usually faster than exact algorithms, and are used when the problem is too large to be solved exactly in a reasonable amount of time. However, they cannot usually guarantee the optimality of the solution found.
Both kind of algorithms can be easily implemented, but the framework is focused on heuristic algorithms, and does not currently provide any exact algorithm implementation.
An example algorithm method is GRASP, which is a heuristic algorithm that iteratively constructs a solution, and then applies an improvement method to each built solution, which is usually a best improvement or first improvement local search.
Its implementation can be as simple as:

```java
public class GRASPAlgorithm<S extends Solution<S, I>, I extends Instance> extends Algorithm<S, I> {
    private static Logger log = LoggerFactory.getLogger(SimpleAlgorithm.class.getName());

    protected final GRASPConstructive<S, I> constructive;
    protected final Improver<S, I> improver;
    
    public GRASPAlgorithm(GRASPConstructive<S, I> constructive, Improver<S, I> improver) { // (1)
        super("GRASP"); // (2)
        this.constructive = constructive;
        this.improver = improver;
    }

    @Override
    public S algorithm(I instance) {
        var solution = this.newSolution(instance); // (3)
        solution = constructive.construct(solution);
        ValidationUtil.assertValidScore(solution); // (4)
        solution = localSearch(solution);
        return solution;
    }

    protected S localSearch(S solution) {
        solution = improver.improve(solution); // (5)
        ValidationUtil.assertValidScore(solution);
        return solution;
    }
}
```

1. Algorithms usually depend on simpler components, and ask for them via its constructor. For example the GRASPAlgorithm needs a GRASPConstructive and an improver component.
2. The algorithm name is passed to the superclass constructor, which is then used for tracking its performance. Algorithm names must be unique per experiment.
3. Any algorithm can create an empty solution by calling the `newSolution(instance)` method. Internally, this method will find the apropiate Java constructor and invoke it. Note that the solution is in its default state, and is probably not feasible.
4. The ValidationUtil methods try to find bugs if the app is running in Validation mode, and do nothing if the app is running in Performance mode. They can be used after each algorithm step to try to find anomalous behaviour that needs to be fixed, without affecting the performance of the application. 
5. The localSearch method is a simple wrapper around the improver component, which is used to improve the solution. The solution is then returned.

Algorithms implemented in the framework follow the [Template Design pattern](https://refactoring.guru/design-patterns/template-method), which means that the algorithm is divided in several steps, and the steps are implemented in different methods. This allows for a high degree of code reuse, as the steps can then be implemented in different classes, or the algorithm can be easily extended by overriding the methods.

Instead of implementing all the required functionality inside this component type, different steps are delegated to different component types, which are declared as dependencies. For example, most heuristic and metaheuristic methods need a constructive method in order to initialize feasible solutions, and usually depend on improvement components in order to get a solution to a local optima. Examples of algorithms are: Scatter Search, Genetic Algorithms, Variable Neighborhood Search and Simulated Annealing. For a full list of available components, see: [Available components](components.md).

### Constructive

Constructive methods receive an empty solution and initialize it. Solutions created by constructive methods must be feasible. Commonly used constructive methods, usually easy to implement, are random and greedy approaches. A more elaborate option is using a GRASP based strategy, see GRASP inside the [algorithm component list](components.md) for more details about GRASP.

### Improver

Improvement methods receive a feasible solutions, and try to improve its objective function score.
Improver methods **cannot** return a solution with worse score that the input solution. 
A classic example of an improver method are local search methods. Note that solutions returned from 
an improvement method are not guaranteed to be optimal, but they are guaranteed to be at least as good as the input solution.
Calling a local search method with a solution that is already at a local optima will return the same solution,
as the local search method will not able to improve it.
Note that the fact that the improvement method returns a solution, does not guarantee that the solution is cloned. 
For performance reasons, the improvement method is allowed to modify the solution in place.
If you want to keep a copy of the original solution, clone it by using the `Solution::cloneSolution` before calling the improvement method.

### Shake

Shake methods (also called perturbation methods in the literature) are implemented similar to improvement methods,
they receive and return a solution, but they are allowed to  return a solution with worse score than the input solution. 
Shake methods try to scape local optima by applying an strategy that may worsen the objective function value
of the given solution. Usually, this type of component is used in combination with improver methods.
A common strategy found in many algorithms consists in iteratively apply a local search method to a solution, to find a local optima, and then apply a shake method to the solution, to try to scape the local optima, and then reapply the improver method.

An example shake strategy, applicable to many optimization problems, is applying random moves to the solution, without checking if the applied moves improve or worsen the objective function value.

Note that the fact that the shake method returns a solution, does not guarantee that the solution is cloned.
For performance reasons, the shake method is allowed to modify the solution in place.
If you want to keep a copy of the original solution, clone it by using the `Solution::cloneSolution` before calling the shake method.


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


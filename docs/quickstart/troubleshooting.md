# Common problems

Explanation of different problems, errors and exceptions that may be found while using the framework.

## The algorithm X does not have public constructors
Algorithms that do not have public constructors use [the builder pattern](https://stackoverflow.com/questions/328496/when-would-you-use-the-builder-pattern). Use the static method, example: `SimulatedAnnealing.builder()`.

## RuntimeException: Could not found Solution constructor Solution(Instance)

By default, the framework finds the solution class implementation, the instance implementation, and automatically initializes solutions under demand using the following solution class constructor:
```java
public class MySolution extends Solution<MySolution, MyInstance>{
    // fields, etc
    
    public MySolution(MyInstance instance){
        // Initialize fields, etc
    }
    
    // other methods
}
```
However, there are certain cases where this strategy will fail:
- If there are multiple classes extending the base Instance.
- If there are multiple classes extending the base Solution.
- If there is no constructor with the structure of the previous code snippet, for example because more parameters are required.

All the previous cases are easily fixed by explaining to the framework how to initialize solutions for an instance.
The only required step is extending the class SolutionBuilder, which determines how solutions are initialized. 
Example:
```java
public class MyProblemSolutionBuilder extends SolutionBuilder<VRPODSolution, VRPODInstance> {
    @Override
    public MySolution initializeSolution(MyInstance instance) {
        return new MySolution(instance, param1, param2, etc);
    }
}
```

If a custom SolutionBuilder implementation is provided by the user, the default strategy is automatically disabled.

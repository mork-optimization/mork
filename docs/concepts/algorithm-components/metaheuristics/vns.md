# Variable Neighborhood Search (VNS)

Variable Neighborhood Search (VNS) is a metaheuristic for solving combinatorial and global optimization problems. It
systematically changes the neighborhood structure during the search to escape local optima and explore the solution
space more effectively.

## Algorithm Outline

The basic VNS algorithm follows this structure:

```
s = GenerateInitialSolution
while (termination criteria not met) {
    k = 1
    while (k != kmax) {
        s' = Shake(s, k)
        s'' = Improve(s')
        NeighborhoodChange(s, s'', k)
    }
}
```

## Main Java Classes

- **`VNS<S, I>`**: Main algorithm class. Generic in solution (`S`) and instance (`I`) types.
- **`VNSBuilder<S, I>`**: Builder for configuring and instantiating VNS algorithms.
- **`VNSNeighChange<S, I>`**: Functional interface for neighborhood change strategies (controls how `k` changes).
- **`DefaultVNSNeighChange<S, I>`**: Default implementation of `VNSNeighChange`, increments `k` until a maximum value is reached.
- **`Constructive<S, I>`**: Interface for generating initial solutions.
- **`Improver<S, I>`**: Interface for local search/improvement.
- **`Shake<S, I>`**: Interface for perturbing solutions.

## How to Use

### 1. Implement or select dependencies

You need to provide implementations for the following three components:

- `Constructive<S, I>`: How to build an initial solution.
- `Improver<S, I>`: How to improve a solution (for example, using a local search).
- `Shake<S, I>`: How to perturb a solution.

Optionally, a custom `VNSNeighChange<S, I>` can be used for customized neighborhood change logic.

### 2. Build VNS Using `VNSBuilder`

The recommended way to initialize and configure the VNS metaheuristic is by using a `VNSBuilder`. Example:
 
```java
var vns = new VNSBuilder<MySolution, MyInstance>()
        .withConstructive(new MyConstructive())
        .withImprover(new MyImprover())
        .withShake(new MyShake())
        // Optionally, configure a custom neighChange method. Example: .withNeighChange(new DefaultVNSNeighChange<>(5, 1))
        .withNeighChange(5) // Uses DefaultVNSNeighChange with kmax=5, increment=1
        // Optionally: change objective to optimize from default to any other
        // .withObjective(myCustomObjective)
        .build("VNS-Config-1");
```


### 3. Custom Neighborhood Change

You can define your own neighborhood change logic by implementing `VNSNeighChange`:

```java
VNSNeighChange<MySolution, MyInstance> customChange = (solution, k) -> {
    if (k >= 7) return VNSNeighChange.STOPNOW;
    return k + 2; // Increase by 2 each time
};
```

!!! tip
    You can quickly implement a custom `VNSNeighChange` to control how the neighborhood change by using a lambda expression.
    The following example is equivalent to the default behavior of `DefaultVNSNeighChange`, with `max=5` and `increment=1`:
    ```java
    .withNeighChange((solution, k) -> k >= 10 ? VNSNeighChange.STOPNOW : k + 1)
    ```

### 6. Implementation notes and tips

- The VNS algorithm stops when the neighborhood change function returns `VNSNeighChange.STOPNOW`.
- k always starts at 0, not 1.
- The `DefaultVNSNeighChange` stops when `k` >= `kmax`.
- If you want to make the VNS multistart, wrap it using the `MultiStartAlgorithm` class.
- Before each shake, the current solution is cloned to avoid worsening it. New solutions are only accepted if they improve the current score.

## References

[1] Hansen P., Mladenović N. (2018) Variable Neighborhood Search. In: Martí R., Pardalos P., Resende M. (eds) Handbook of
Heuristics. Springer, Cham. [DOI: 10.1007/978-3-319-07124-4_19](https://doi.org/10.1007/978-3-319-07124-4_19)
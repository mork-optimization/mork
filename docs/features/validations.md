# Validation and testing

While developing a new optimization problem it is easy to introduce subtle bugs: an objective
function that is not correctly updated after a move, a constructive method that returns an infeasible
solution, a neighborhood that generates invalid moves, etc. Mork provides a validation mechanism that
automatically checks solutions **during execution**, so these bugs surface as early as possible
instead of silently corrupting your results.

## Validation mode vs performance mode

Mork can run in two modes:

- **Validation mode**: the framework runs a series of correctness checks after each algorithm step.
  Use it while developing and debugging.
- **Performance mode**: all checks are skipped so the algorithm runs at full speed. Use it to run
  the final experiments.

Validation is implemented on top of **Java assertions**: the framework triggers its checks through
`assert Context.validate(solution)` statements, which the JVM only executes when assertions are
enabled.

!!! warning
    By default, the JVM runs with assertions **disabled** (i.e. performance mode). To enable
    validation mode, pass the `-ea` (`-enableassertions`) flag to the JVM, for example:
    ```bash
    java -ea -jar myproject.jar
    ```
    Most IDEs let you add `-ea` to the run configuration VM options. Remember to disable it before
    running your performance experiments.

## What is validated

When validation runs, `Context.validate(solution)`:

1. Runs your custom `SolutionValidator`, if you implemented one (see below).
2. Checks basic framework invariants (for example, that the tracked *time to best* is consistent).
3. If reference results are available for the instance, checks the reported objective values against
   them.

If any check fails, Mork throws an `InvalidSolutionException` describing the reason and stops the
execution, so the bug cannot go unnoticed.

## Implementing a solution validator

To add problem-specific checks, extend `SolutionValidator<S, I>` and implement the `validate`
method. Mork automatically detects and registers your validator — there is nothing else to wire up.

```java
public class MySolutionValidator extends SolutionValidator<MySolution, MyInstance> {

    @Override
    public ValidationResult validate(MySolution solution) {
        var result = ValidationResult.ok();

        // Check any invariant that must always hold for a feasible solution.
        // These checks are problem specific: use your own solution/instance methods.
        if (!solution.isComplete()) {
            result.addFailure("Solution is not complete: some elements remain unassigned");
        }
        if (solution.hasDuplicatedElements()) {
            result.addFailure("Solution contains duplicated elements");
        }

        return result;
    }
}
```

The `ValidationResult` API:

- `ValidationResult.ok()`: start from a valid result.
- `ValidationResult.fail(reason)`: build an invalid result with a single reason.
- `addFailure(reason)`: accumulate several failure reasons into the same result.
- `isValid()` / `getReasonsFailed()`: inspect the outcome.

!!! tip
    A validator is the ideal place to recompute the objective function from scratch (using
    `Context.getMainObjective().evalSol(solution)`) and compare it against the value your solution
    maintains incrementally. This catches the most common class of bug — moves that forget to update
    the objective — and, because these checks only run in validation mode, you can afford expensive
    from-scratch recomputations here without slowing down your final experiments.

## Unit testing

Solution validators complement, but do not replace, traditional unit tests. Mork projects are plain
Maven projects, so you can add JUnit tests for your instances, moves, constructives, and objective
functions as you would in any Java project. The generated project already includes a `src/test`
folder set up for this purpose.

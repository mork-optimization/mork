# Time limits

Mork can give each algorithm execution a time budget. Custom algorithms and components check `TimeControl` so they stop when it expires.

## How time limits work

When time control is enabled, each combination of instance, algorithm, and repetition receives its own budget.
All components called during that execution share the same deadline.

Time control measures elapsed time since the algorithm started executing using the monotonic `System.nanoTime()` clock. Waiting, JVM pauses, and
time spent inside components count towards the budget. Instance loading and output serialization are always outside this budget.

**Time limits are cooperative.** Mork does not interrupt or kill an algorithm when its budget expires.
Components must check `TimeControl.isTimeUp()` and return promptly. An operation that never checks the
deadline can continue indefinitely.

## Setting a limit for normal experiments

Create one public class extending `TimeLimitCalculator` in a package scanned by your application. Mork
discovers the implementation through the inherited component annotation and uses it automatically for
normal experiments.

Replace `MySolution` and `MyInstance` with your problem's classes:

```java
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;

public class MyTimeLimit extends TimeLimitCalculator<MySolution, MyInstance> {

    @Override
    public long timeLimitInMillis(MyInstance instance, Algorithm<MySolution, MyInstance> algorithm) {
        return 60_000L; // 60 seconds for each execution
    }
}
```

The return value is in **milliseconds**. It can depend on the instance, the algorithm, or configuration
injected into your calculator. For example, larger instances can receive a longer budget. See
[App configuration](config.md) for ways to provide configuration values at runtime.

## Respecting the limit in custom components

Check `TimeControl.isTimeUp()` in expensive loops and before starting costly phases. Built-in
algorithms commonly check between iterations, but a custom component called within an iteration must
also check during its own work.

For example, an improver can stop between improvement passes:

```java
@Override
public S improve(S solution) {
    boolean improved = true;
    while (improved && !TimeControl.isTimeUp()) {
        improved = improveStep(solution);
    }
    return solution;
}
```

Here, `improveStep` is a single step in the improvement method. If one pass can take substantial time, check the deadline inside that helper
as well, for example while scanning candidate moves, and return early if you have run out of time.

Return the best valid solution available when time expires. An improver should preserve its input's quality
contract, and a constructive must finish with a feasible solution. Choose a budget that allows the first
feasible solution to be built, and avoid leaving a partially applied move or incomplete reconstruction when
stopping. See the [improver guidelines](../concepts/algorithm-components/improvers/improver.md).

Time expiry is one stopping criterion. Algorithms may finish earlier because they reached an iteration
limit, found a local optimum, or met another condition.

## Time limits during tuning

### Manual IRACE

For manual tuning through an `AlgorithmBuilder`, time control is disabled by default, even if your project
contains a `TimeLimitCalculator`. Enable it with:

```yaml
irace:
  timecontrol: true
```

Each evaluation then uses your calculator's budget. Enabling `irace.timecontrol` without a calculator causes
an exception. With the property set to `false`, evaluations rely on the algorithm's own stopping criteria.
See [IRACE integration](irace.md) for the manual tuning workflow.

### Automatic configuration

In automatic configuration, Mork always enables time control and sets each evaluation's budget to:

```text
solver.ignore-initial-millis + solver.interval-duration-millis
```

This mode uses that combined budget regardless of `TimeLimitCalculator` or `irace.timecontrol`. The default
timing configuration is:

```yaml
solver:
  ignore-initial-millis: 10000
  interval-duration-millis: 50000
  log-scale-area: true
  autorestart: true
```

These values give each evaluation 60 seconds. The first 10 seconds allow the algorithm to establish a
solution; the objective curve is scored over the following 50 seconds using its area under the curve (AUC).
The best value established before the scoring interval becomes its initial value, and improvements after
the interval ends do not affect the score. `log-scale-area` applies the natural logarithm to the area.

Automatic configuration always enables metrics. Custom algorithms must report an objective value before the
scoring interval starts and record subsequent improvements, for example with
`Metrics.addCurrentObjectives(bestSolution)`. If the curve cannot be scored, Mork rejects the evaluation.
See [Metrics](serialization.md#metrics) for reporting objective values.

With `solver.autorestart: true`, Mork wraps the selected algorithm in a multi-start algorithm, restarting it
when it finishes early. Restarts share the original deadline. Set it to `false` to evaluate a single invocation;
if it finishes early, its last recorded objective value is carried forward through the rest of the scoring
interval. See [Autoconfig](autoconfig.md) for automatic configuration setup.

### Total tuning budget

The duration of one algorithm evaluation is separate from IRACE's budget for the whole tuning run.
Changing `irace.maxExperiments` or the scenario's overall time budget does not configure a per-evaluation
deadline. See [scenario configuration](irace.md#adjusting-scenario-options) and the
[autoconfig REST API](autoconfig-rest-api.md#run-status) for tuning budget configuration and monitoring.

## Using the TimeControl API

Import `es.urjc.etsii.grafo.util.TimeControl` in custom components.

| Method                            | Behavior                                                                                                                                                                                                       |
|-----------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `isTimeUp()`                      | Returns whether the current execution's budget has expired. Always returns `false` if time control is disabled.                                                                                                |
| `isEnabled()`                     | Returns whether time control has been started for the current thread. It remains `true` after expiry until cleanup. Note that in most cases you do not need to call this method, using isTimeUp may be enough. |
| `remaining()`                     | Returns the remaining time in **nanoseconds**. A negative value indicates an overrun. Throws `IllegalStateException` when TimeControl is disabled.                                                                            |
| `setMaxExecutionTime(time, unit)` | Sets the duration using a `java.util.concurrent.TimeUnit`. Does not start the clock.                                                                                                                           |
| `start()`                         | Enables time control and starts the clock from the current instant.                                                                                                                                            |
| `remove()`                        | Removes time control from the current thread.                                                                                                                                                                  |

`setMaxExecutionTime`, `start` and `remove` are methods used by the framework to set up the experiments, and should not be called from algorithm components.
Note that you can run your algorithms outside the Mork framework, configuring the duration by call `start()` and `setMaxExecutionTime(time, unit)`, and calling
`remove()` in a `finally` block when finished.

Before calling `remaining()`, you must check `isEnabled()`. Convert nanoseconds with
`TimeUnit.NANOSECONDS.toMillis(...)` when passing a budget to an external solver that expects milliseconds.
Clamp an operation's own limit to the remaining execution budget, and stop if the resulting duration is
zero or negative. [CMSA](../concepts/algorithm-components/metaheuristics/cmsa.md) does this when calling
its solver; custom external solver integrations must pass and respect their own timeout.

## Warm-up limits
TimeControl can be used for warming up the JVM before running the main experiments:

```yaml
solver:
  warmup:
    enabled: true
    repetitions: 5
    max-millis: 1000
```

A positive `solver.warmup.max-millis` overrides the calculator for each warm-up execution, even when no
calculator exists. In this example, each algorithm receives one second per warm-up repetition. It is still
a cooperative limit, so warm-up components must check the deadline.

Set `max-millis: 0` to use normal behavior: the calculator's budget if present, or no framework time limit
otherwise. This setting affects warm-up only; measured repetitions use their normal budgets. See
[JVM warm-up instance selection](instance-manager.md#jvm-warm-up-instance) for selecting a suitable instance.

## Parallel execution and custom threads

Mork initializes time control independently for each execution in both sequential and parallel executors.
Running several instances concurrently gives each its own elapsed-time budget. Contention for CPU time
still consumes that budget, so a big parallel load can therefore degrade the algorithm performance when using a time limit.

Internally, TimeControl is implemented using an `InheritableThreadLocal`. This means that if an algorithm components creates a thread, the newly created child thread inherits the parent's time
status and shares its deadline, provided the thread is created after time control has been initialized.
Note that because inheritance happens when the thread is created, submitting a task to an existing thread pool does not transfer
the submitting execution's current time status.

## Troubleshooting overruns

If an execution runs longer than its budget, check the active execution mode, the calculator's units, and
whether expensive components poll the deadline. A check in an outer loop cannot stop a blocking external
call, a long neighborhood scan, or a constructive that never checks time. Pass a timeout to external
operations and check between smaller units of work where possible.

Mork logs `Algorithm takes too long to stop after time is up` when an execution returns more than 10 seconds
after its deadline. This check happens after the algorithm returns; it is not a watchdog or an additional
10 seconds of permitted search. Tuning evaluations also expose this overrun through their slow-execution
details in the [autoconfig REST API](autoconfig-rest-api.md#evaluations).

A warning does not forcibly terminate the algorithm or reject an otherwise valid result solely for taking
too long. When running in automatic mode, improvements after the timelimit or scoring interval are ignored.

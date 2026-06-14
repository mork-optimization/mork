# Mork Benchmarks

This module contains JMH benchmarks for comparing CPU time between Java
`HashSet<Integer>` and Mork's integer `BitSet` implementation.

The benchmark data is deterministic. For every `(universeSize, fillRatio)` pair,
the input elements are sampled without replacement, so the configured fill ratio
matches the actual set cardinality.

## Benchmarks

### SetConstructionBenchmark

Builds a new set from the same unique integer elements.

### SetContainsBenchmark

Runs lookup batches against pre-built sets. The `hitRatio` parameter controls
how many lookup keys are present in the set.

### SetIterationBenchmark

Iterates over a pre-built set and consumes every stored integer.

## Build

Requirements:

- Java 25 or newer for building and running the benchmarks.
- Maven wrapper from the repository root.

From the repository root:

```bash
./mvnw -pl benchmarks -am package -DskipTests
```

On Windows:

```powershell
.\mvnw.cmd -pl benchmarks -am package "-DskipTests"
```

The runnable JMH jar is created at:

```text
benchmarks/target/benchmarks.jar
```

## Run

List available benchmarks:

```bash
java -jar benchmarks/target/benchmarks.jar -l
```

If `java` on your `PATH` points to an older runtime, use the Java 25+ executable
directly, for example:

```bash
$JAVA_HOME/bin/java -jar benchmarks/target/benchmarks.jar -l
```

Run the full benchmark suite:

```bash
java -jar benchmarks/target/benchmarks.jar
```

Run one benchmark with selected parameters:

```bash
java -jar benchmarks/target/benchmarks.jar SetContainsBenchmark -p universeSize=16384 -p fillRatio=0.50 -p hitRatio=0.50
```

Use more forks or iterations for decision-grade results:

```bash
java -jar benchmarks/target/benchmarks.jar -f 5 -wi 10 -i 20 -rf json -rff benchmark-results.json
```

## Parameters

- `universeSize`: maximum number of integers representable in the benchmark set.
- `fillRatio`: fraction of the universe stored in the set.
- `hitRatio`: fraction of lookup keys that are present in the set, only used by `SetContainsBenchmark`.

## Notes

- These benchmarks report CPU timing only.
- Run benchmarks on an idle machine and on the target Java/runtime version.
- Treat results as comparative data for these integer-set workloads, not as a universal ranking of either collection type.

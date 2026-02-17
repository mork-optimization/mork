# Mork Benchmarks

This module contains JMH (Java Microbenchmark Harness) benchmarks for the Mork framework, focusing on performance comparisons between the standard Java `HashSet` and the custom `BitSet` implementation.

## Benchmark Tests

### SetAddBenchmark
Compares the performance of adding elements to HashSet vs BitSet across different data sizes (100, 1000, 10000 elements) and fill ratios (50%, 90%).

**Measures:** Average time per operation in nanoseconds

### SetContainsBenchmark
Compares the performance of lookup/contains operations between HashSet and BitSet.

**Measures:** Average time for 1000 lookups in nanoseconds

### SetIterationBenchmark
Compares the performance of iterating over all elements in HashSet vs BitSet.

**Measures:** Average time to iterate over all elements in nanoseconds

### SetRemoveBenchmark
Compares the performance of removing elements from HashSet vs BitSet.

**Measures:** Average time per operation in nanoseconds

### SetMemoryBenchmark
Measures memory allocation patterns between HashSet and BitSet across different sizes (1000, 10000, 100000 elements).

**Measures:** Average and sample time, allocation rate tracking

### SetMixedOperationsBenchmark
Simulates realistic workloads with mixed operations (add, contains, remove) to compare overall performance.

**Measures:** Average time per mixed workload in microseconds

## Building the Benchmarks

To build the benchmark uber JAR:

```bash
cd benchmarks
mvn clean package
```

This will create `target/benchmarks.jar` containing all benchmarks and dependencies.

## Running Benchmarks

### Run All Benchmarks

```bash
java -jar target/benchmarks.jar
```

### Run Specific Benchmark

```bash
# Run only add benchmarks
java -jar target/benchmarks.jar SetAddBenchmark

# Run only contains benchmarks
java -jar target/benchmarks.jar SetContainsBenchmark
```

### Run with Custom Parameters

```bash
# Run with specific parameters
java -jar target/benchmarks.jar SetAddBenchmark -p size=1000 -p fillRatio=0.9

# Run with specific warmup and measurement iterations
java -jar target/benchmarks.jar -wi 5 -i 10
```

### Run with Profilers

JMH includes several profilers to get additional insights:

```bash
# List available profilers
java -jar target/benchmarks.jar -lprof

# Run with GC profiler
java -jar target/benchmarks.jar -prof gc

# Run with stack profiler
java -jar target/benchmarks.jar -prof stack

# Run with allocation profiler (requires -javaagent)
java -jar target/benchmarks.jar -prof "async:libPath=/path/to/libasyncProfiler.so;output=flamegraph"
```

### Save Results

```bash
# Save results to JSON
java -jar target/benchmarks.jar -rf json -rff results.json

# Save results to CSV
java -jar target/benchmarks.jar -rf csv -rff results.csv
```

## Understanding Results

The benchmarks measure:

- **CPU Performance**: All benchmarks measure execution time (throughput)
- **Memory Impact**: SetMemoryBenchmark runs with specific GC settings to track allocation patterns
- **Different Use Cases**:
  - Small datasets (100-1000 elements): Typical for small optimization problems
  - Medium datasets (10000 elements): Common in many algorithms
  - Large datasets (100000 elements): Stress testing for memory benchmarks

### Expected Results

**BitSet Advantages:**
- Lower memory footprint for dense sets (high fill ratios)
- Faster operations when elements are small integers
- More predictable performance characteristics

**HashSet Advantages:**
- Better for sparse sets (low fill ratios)
- Can handle any object type, not just integers
- No capacity limit requirement

## JMH Options

Common JMH command-line options:

- `-h`: Display help
- `-l`: List available benchmarks
- `-lprof`: List available profilers
- `-wi <count>`: Number of warmup iterations
- `-i <count>`: Number of measurement iterations
- `-f <count>`: Number of forks
- `-t <count>`: Number of threads
- `-p <param>=<value>`: Set benchmark parameter

## Requirements

- Java 21 or higher
- Maven 3.6+
- At least 2GB of available memory for running benchmarks

## Notes

- Benchmarks may take several minutes to complete
- Results can vary based on JVM version, hardware, and system load
- For production decisions, always run benchmarks on target hardware
- The benchmarks use a fixed random seed (42) for reproducibility

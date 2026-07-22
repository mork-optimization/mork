# Mork Benchmarks

This module contains JMH benchmarks.

## Benchmarks

### HashSet<Integer> vs Mork BitSet
Comparing elapsed time (wall-clock) between Java
`HashSet<Integer>` and Mork's integer `BitSet` implementation.

The benchmark data is deterministic. For every `(universeSize, fillRatio)` pair,
the input elements are sampled without replacement, so the configured fill ratio
matches the actual set cardinality.

### Moocore Pareto algorithms

`MoocoreBenchmark` covers Pareto filtering, early dominated-point detection,
Pareto ranking, exact and approximate hypervolume, weighted and HypE
hypervolume, EAF, and nondominated-set generation. The Pareto stress states use
deterministic random, mutually nondominated simplex, and dominance-chain
inputs. They cover 4D, 5D, and 9D and include the 500, 1,000, 2,000, and
4,000-point scaling sizes.

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
java --add-modules jdk.incubator.vector -jar benchmarks/target/benchmarks.jar -l
```

If `java` on your `PATH` points to an older runtime, use the Java 25+ executable
directly, for example:

```bash
$JAVA_HOME/bin/java --add-modules jdk.incubator.vector -jar benchmarks/target/benchmarks.jar -l
```

Run the full benchmark suite:

```bash
java --add-modules jdk.incubator.vector -jar benchmarks/target/benchmarks.jar
```

Run one benchmark with selected parameters:

```bash
java --add-modules jdk.incubator.vector -jar benchmarks/target/benchmarks.jar SetContainsBenchmark -p universeSize=16384 -p fillRatio=0.50 -p hitRatio=0.50
```

Run the high-dimensional Pareto mask and allocation cases:

```bash
java --add-modules jdk.incubator.vector -jar benchmarks/target/benchmarks.jar MoocoreBenchmark.nondominatedStress -p objectives=5,9 -p shape=RANDOM,SIMPLEX -prof gc
```

Use more forks or iterations for decision-grade results:

```bash
java --add-modules jdk.incubator.vector -jar benchmarks/target/benchmarks.jar -f 5 -wi 10 -i 20 -rf json -rff benchmark-results.json
```

## Parameters

- `universeSize`: maximum number of integers representable in the benchmark set.
- `fillRatio`: fraction of the universe stored in the set.
- `hitRatio`: fraction of lookup keys that are present in the set, only used by `SetContainsBenchmark`.
- Pareto `size`: number of points.
- Pareto `objectives`: point dimension.
- Pareto `shape`: `RANDOM` or mutually nondominated `SIMPLEX`.

## Notes

- These benchmarks report elapsed timing only.
- Run benchmarks on an idle machine and on the target Java/runtime version.
- Treat results as comparative data for these integer-set workloads, not as a universal ranking of either collection type.

## Native moocore cross-language comparison

The cross-language harness compares `mork-moocore` with the Python `moocore`
public API backed by the native C implementation. It keeps the pinned
`moocore` submodule unchanged and covers nondominance, Pareto ranking, exact
hypervolume, hypervolume approximation, additive epsilon, and IGD+.

The approximation matrix uses the Linear 3D, 4D, 6D, and 9D datasets and the
Sphere 6D 1,000-point dataset available in the pinned testsuite. The latter is
the explicit replacement for the missing
`DTLZSphereShape.6d.front.500pts.10.xz`. The upstream Python script also
references `ran.800pts.6d.10`, `ran.80pts.9d.10`, and
`DTLZSphereShape.10d.front.150pts.10`; those files are not present in the
pinned testsuite, so those three cases are omitted.

Java is measured with JMH. Python and its C extensions are measured in two
fresh Python worker processes with calibrated batches. Both sides use three
one-second warmup iterations followed by five one-second measurement
iterations. There is no short or reduced measurement profile.
The complete matrix may take several hours, depending on the CPU and on when
the harness drops later sizes after a Python call exceeds its ten-second
single-call limit.

### Set up

Initialize the pinned submodules, then run the cross-platform setup script from
the repository root:

```bash
git submodule update --init moocore testsuite
python3 benchmarks/cross-language/setup_environment.py
```

The script requires Java 25 or newer. It creates the ignored virtual
environment `benchmarks/cross-language/venv`, installs every dependency from
`benchmarks/cross-language/requirements.txt`, verifies the Python/C API,
and builds `benchmarks/target/benchmarks.jar`. Running it again safely updates
the environment and rebuilds the JAR.

The equivalent manual environment commands on Unix-like systems are:

```bash
python3 -m venv benchmarks/cross-language/venv
benchmarks/cross-language/venv/bin/python -m pip install \
    -r benchmarks/cross-language/requirements.txt
./mvnw -pl benchmarks -am -DskipTests clean package
```

All dependencies are mandatory. If the runner finds a missing package, it
stops before collecting measurements and prints the setup and manual commands.
The requirements intentionally contain no competing multi-objective Python
packages; only local Python/C `moocore` and reporting utilities are installed.

### Run

The setup script prints the platform-specific command. On Unix-like systems it
is:

```bash
benchmarks/cross-language/venv/bin/python \
    benchmarks/cross-language/run.py
```

All six benchmark families run by default. To run selected families with the
same full measurement configuration:

```bash
benchmarks/cross-language/venv/bin/python \
    benchmarks/cross-language/run.py \
    --operations nondominated hypervolume
```

The runner prints timestamped phase and per-case progress. Python worker JSON
is written after each complete fork, while JMH writes one raw result file per
operation.

Use `--output` to select the result directory and `--java-jar` to use another
JMH artifact. Generated inputs and results default to the ignored directory
`benchmarks/target/cross-language`.

Performance packages use a fixed representative checkpoint matrix. The first
checkpoint measures both runtimes; later checkpoints reuse the unchanged
native measurements:

```shell
benchmarks/cross-language/venv/bin/python benchmarks/cross-language/run.py \
  --quick --output benchmarks/target/cross-language/stage-1-baseline \
  --java-jar benchmarks/target/cross-language/jars/stage-1-baseline.jar

benchmarks/cross-language/venv/bin/python benchmarks/cross-language/run.py \
  --quick --reuse-python benchmarks/target/cross-language/stage-1-baseline \
  --output benchmarks/target/cross-language/stage-2-sort-pareto \
  --java-jar benchmarks/target/cross-language/jars/stage-2-sort-pareto.jar
```

Omit `--quick` and `--reuse-python` for the full final/overnight matrix.

### Timing and correctness boundaries

Each input is prepared once, serialized as a little-endian binary matrix, and
read by both runtimes. Dataset loading, filtering, deduplication,
representation conversion, process startup, JVM startup, and JIT warmup are
outside the measurement. Public API dispatch, computation, and returned-result
allocation are included. No JNI bridge is involved.

Python `moocore` produces the correctness oracle before timing. Nondominance
masks and Pareto ranks must match exactly. Deterministic floating-point results
must match with relative tolerance `1e-9` and absolute tolerance `1e-12`.
DZ2019-MC uses 262,144 samples and seed 42; because C and Java deliberately use
different random streams, both results are checked independently against exact
hypervolume with a 5% relative-error bound.

The harness writes:

- `raw/`: per-fork Python samples and unmodified JMH JSON.
- `results.json` and `results.csv`: normalized nanoseconds per operation.
- `report.md`: absolute times and ratios to Python `moocore`.
- `plots/`: one comparison plot per operation and dataset.
- `environment.json`: CPU, OS, Java, Python, package, compiler, and Git revision
  metadata.

The default matrix covers nondominance, Pareto rank, exact and approximate
hypervolume, hypervolume contributions, additive and multiplicative epsilon,
IGD+, exact R2, EAF and EAF differences, rectangle-weighted hypervolume, and
HypE weighted hypervolume.

Performance work is recorded as named checkpoints. After running a checkpoint,
collect representative Java allocations with:

```shell
benchmarks/cross-language/venv/bin/python \
  benchmarks/cross-language/profile_gc.py \
  --stage benchmarks/target/cross-language/stage-1-baseline \
  --java-jar benchmarks/target/benchmarks.jar
```

Regenerate the tracked cumulative report by passing every checkpoint in order:

```shell
benchmarks/cross-language/venv/bin/python \
  benchmarks/cross-language/update_performance_report.py \
  --stage baseline=benchmarks/target/cross-language/stage-1-baseline \
  --stage shared-kernels=benchmarks/target/cross-language/stage-2-shared-kernels
```

The report compares each checkpoint and each package's targeted operations directly
with C+Python. A Java/C+Python ratio below 1.0 favors Java; a ratio above 1.0 favors
C+Python. Allocation ratios use the Java baseline because the native harness does not
provide equivalent allocation measurements.

Run comparisons on an idle target machine. Compare implementations only within
the same generated report; results from different machines, resolved package
versions, JVMs, or compiler builds are not directly comparable.

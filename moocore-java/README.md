# Mork moocore

`mork-moocore` is a pure Java implementation of the public API of
[`moocore`](https://github.com/multi-objective/moocore). It does not depend or need either JNI nor the Foreign
Function & Memory API.

> [!WARNING]
> This module is still experimental, and it is currently being tested. Use at your own risk.
> Report any issues or inconsistencies detected so we can work on fixing them.


To use this module, add the following dependency to your `pom.xml` file:

```xml

<dependency>
    <groupId>es.urjc.etsii.grafo</groupId>
    <artifactId>mork-moocore</artifactId>
    <!-- First public release in Maven will be 0.23, 
    if you want to test it before you need to
    clone and install a local Mork development version -->
    <version>0.23-SNAPSHOT</version>
</dependency>
```

## Quick start

The API uses ordinary row-major Java arrays. Inputs are defensively copied when they are retained and operations do not
mutate caller-owned matrices.

```java
double[][] front = {{5, 5}, {4, 6}, {2, 7}, {7, 4}};

double hypervolume = MooCore.hypervolume(front, new double[]{10, 10});
boolean[] nondominated = MooCore.isNondominated(front);
double igdPlus = MooCore.igdPlus(front, new double[][]{{2, 7}, {7, 4}});
```

Pass one direction to apply it to every objective, or one value per objective:

```java
double value = MooCore.hypervolume(
        front,
        new double[]{0},
        new boolean[]{true});
```

Set-aware operations use a parallel set-identifier array or `Dataset`:

```java
Dataset runs = MooCore.getDataset("input1.dat");
double[][] attainment = MooCore.eaf(runs.points(), runs.sets(),
        new double[]{25, 50, 75});
```

## Implemented API

- Dominance checks, filtering, Pareto ranks, and within-set variants.
- IGD, IGD+, average Hausdorff distance, additive and multiplicative epsilon, exact two-dimensional R2, and reusable
  hypervolume indicators.
- Exact hypervolume and contributions, plus DZ2019-MC, DZ2019-HW, and Rphi-FWE+ approximations.
- Two- and three-dimensional EAF surfaces; two-dimensional EAF differences, Vorob'ev expectation/deviation, and largest
  EAF difference.
- Rectangle-weighted and total weighted hypervolume, and HypE Monte Carlo weighting.
- Normalisation and six random nondominated-set shapes, including integer output.
- Blank-line/comment-separated dataset parsing, XZ input, a checksummed local cache, and retrying downloads for large
  testsuite datasets.

Exact hypervolume supports 1 to 31 objectives. Distance and epsilon indicators have no objective-count ceiling;
normalisation, dominance, and Pareto ranking support up to 255 objectives. EAF is defined here for two or three
objectives; EAF differences and weighted methods are currently two-dimensional, matching upstream's current
restrictions.

The deterministic quasi-Monte Carlo methods reproduce upstream numerical results within floating-point tolerance. 
Stochastic methods are reproducible within Java, but do not promise the same random stream as C, Python, or R.
The approximation kernels use Java 25's incubating Vector API directly. Applications that call them must start the
JVM with `--add-modules jdk.incubator.vector`; the module's Maven test and benchmark configurations already do this.

`input1.dat` and `ran.10pts.9d.10` are packaged for offline use. Both come from the MPL-2.0 testsuite repository.
Python-package-only datasets are deliberately not redistributed because that package is LGPL-2.1-or-later; they can
still be read from a user-supplied path.

## Verification and benchmarks

```shell
./mvnw -pl moocore-java -am test
git submodule update --init moocore testsuite
./mvnw -pl moocore-java -am -Pmoocore-testsuite verify
./mvnw -pl benchmarks -am -DskipTests package
java --add-modules jdk.incubator.vector -jar benchmarks/target/benchmarks.jar MoocoreBenchmark.nondominated
java --add-modules jdk.incubator.vector -jar benchmarks/target/benchmarks.jar MoocoreBenchmark.nondominatedStress -prof gc
```

Tests cover published examples, documented Python outputs, randomized cross-checks against simple definitions, mixed
objective directions, packaged data, and edge cases. JMH cases cover dominance, ranking, exact hypervolume, and EAF
across representative sizes and dimensions, including random, mutually nondominated simplex, and dominance-chain
stress inputs in 4D, 5D, and 9D.

The `moocore-testsuite` Maven profile also runs every recipe in the pinned upstream testsuite submodule: 107 recipes are
checked against their numerical oracles and the two recipes without stable textual output check EAF polygon invariants.
The profile verifies the expected testsuite Git revision and fails if any upstream `.test` recipe is absent from the
explicit coverage manifest. To isolate a recipe or family while debugging, set a path substring, for example
`-Dmoocore.testsuite.case=hv/`.

The dedicated `moocore Java upstream testsuite` workflow initializes both submodules and runs this profile whenever the
implementation, either submodule pin, its parent POM, or the workflow changes. The complete 109-recipe manifest runs
in one job.

`tools/generate_reference_fixtures.py` can regenerate deterministic oracle values with an installed Python `moocore`.
From the repository root, run:

```shell
python3 moocore-java/tools/generate_reference_fixtures.py \
    moocore-java/target/python-reference-fixtures.json
```

The command uses the `moocore` installed in that Python environment and records its version in the
`upstream_version` field. To use the Python binding from the pinned `moocore` submodule, install it first with
`python3 -m pip install ./moocore/python`.

The generated JSON is a review artifact; Maven does not read it automatically and the Java tests do not require
Python. Compare its deterministic values with the expected values in
`src/test/java/es/urjc/etsii/grafo/moocore/MooCoreTest.java`. The hypervolume, contribution, and R2 fields correspond to
`computesPublishedIndicators`; the approximation fields correspond to `matchesDeterministicHypervolumeApproximations`;
and the EAF-difference fields correspond to `matchesDocumentedEafDifferenceOutputs`. If an intentional upstream change
requires updating those constants, review the numerical difference, copy the accepted values into the corresponding
test, and verify them with:

```shell
./mvnw -pl moocore-java -am test
```

Do not use this process to update stochastic expectations: Java methods deliberately use a different random
stream from C, Python, and R.

For native performance comparisons against the Python `moocore` public API
backed by its C implementation, follow the
[cross-language benchmark instructions](../benchmarks/README.md#native-moocore-cross-language-comparison).
The harness supplies identical prepared inputs to Python/C and Java, verifies
the outputs before timing, and merges their native-runtime measurements without
JNI or process-startup overhead.

## License

Note that this module is licensed under MPL-2.0, as it is based on and would not be possible without the work of:

- 2005 Carlos M. Fonseca.
- 2006-2008, 2015, 2025 Carlos M. Fonseca and Manuel Lopez-Ibanez.

Other Mork modules remain under their existing license.

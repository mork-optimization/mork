# Mork moocore

`mork-moocore` is a pure Java implementation of the public API of
[`moocore`](https://github.com/multi-objective/moocore). It does not depend or need either JNI nor the Foreign
Function & Memory API.

> [!WARNING]
> This module is still experimental, and it is being currently being tested. Use at your own risk.
> Do report any issues or inconsistencies detected so we can work on fixing them.


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

The deterministic quasi-Monte Carlo methods reproduce upstream numerical results within floating-point tolerance. Seeded
stochastic methods are reproducible within Java, but do not promise the same random stream as C, Python, or R.

`input1.dat` and `ran.10pts.9d.10` are packaged for offline use. Both come from the MPL-2.0 testsuite repository.
Python-package-only datasets are deliberately not redistributed because that package is LGPL-2.1-or-later; they can
still be read from a user-supplied path.

## Verification and benchmarks

```shell
./mvnw -pl moocore-java -am -Dgpg.skip test
git submodule update --init moocore testsuite
./mvnw -pl moocore-java -am -Pmoocore-testsuite -Dgpg.skip verify
./mvnw -pl benchmarks -am -DskipTests package
java -jar benchmarks/target/benchmarks.jar MoocoreBenchmark.nondominated
java -jar benchmarks/target/benchmarks.jar MoocoreBenchmark.nondominatedStress -prof gc
```

Tests cover published examples, documented Python outputs, randomized cross-checks against simple definitions, mixed
objective directions, packaged data, and edge cases. JMH cases cover dominance, ranking, exact hypervolume, and EAF
across representative sizes and dimensions, including random, mutually nondominated simplex, and dominance-chain
stress inputs in 4D, 5D, and 9D.

The `moocore-testsuite` Maven profile also runs every recipe in the pinned upstream testsuite submodule: 107 recipes are
checked against their numerical oracles and the two recipes without stable textual output check EAF polygon invariants.
The profile verifies the expected testsuite Git revision and fails if any upstream `.test` recipe is absent from the
explicit coverage manifest. To isolate a recipe or family while debugging, set a path substring, for example
`-Dmoocore.testsuite.case=hv/`. The stable manifest can also be split across parallel runners with zero-based shards
such as
`-Dmoocore.testsuite.shard=0/4`.

The dedicated `moocore Java upstream testsuite` workflow initializes both submodules and runs this profile whenever the
implementation, either submodule pin, its parent POM, or the workflow changes. It runs four deterministic shards in
parallel; together they cover the same 109-recipe manifest.

`tools/generate_reference_fixtures.py` can regenerate deterministic oracle values with an installed Python `moocore`. It
is a development tool only; the Java artifact and its tests do not depend on Python.

## License

Note that this module is licensed under MPL-2.0, as it is based on and would not be possible without the work of:

- 2005 Carlos M. Fonseca.
- 2006-2008, 2015, 2025 Carlos M. Fonseca and Manuel Lopez-Ibanez.

The original projects uses the LGL due to the AVL files, we do not need to use the LGLP because we use standard Java data structures instead.
Other Mork modules remain under their existing license.

# Space-free facility layout

This Java example solves the space-free facility layout problem. Facilities are
assigned to ordered rows, and each row is packed consecutively from coordinate
zero. The objective minimizes the sum of pairwise flow multiplied by the absolute
distance between facility centers.

The model supports multiple rows, while the current instance importer and default
experiment dataset are restricted to **two rows**. The selected instances are in
`instances/2rows/`. The other instance files describe layouts with more rows and
are not loaded by the default configuration.

## Build and run

From the Mork repository root:

```bash
mvn -pl example-flayouts -am -DskipTests package
```

Run from this module's directory so that relative instance paths resolve correctly:

```bash
cd example-flayouts
java -jar target/flayouts-0.23-SNAPSHOT.jar --solver.experiments=SpaceFreeLayoutExperiment
```

`SpaceFreeLayoutExperiment` runs random construction followed by best-improvement
relocation search. It is a runnable example, not a reproduction of the published
reference algorithm. Add algorithms to its `getAlgorithms()` method or declare
another class extending `AbstractExperiment<FLPSolution, FLPInstance>`.

For automatic configuration, use the same JAR with `--autoconfig`. Components are
discovered from their annotations and the factories in `FLPOriginalFactoriesNew`.
`FLPExplorationFilterNew` restricts their composition to keep the parameter space
bounded. The random constructives are named `FLPRandomConstructive` and
`FLPRandomConstructiveNew`; saved configurations must use these current names.
Regenerate `parameters.txt` when changing the available components.

## Instance format

Whitespace-separated integers specify:

1. The number of rows, currently required to be `2` by the importer.
2. The number of facilities, `N`.
3. `N` facility widths.
4. An `N × N` matrix of pairwise flows.

For example:

```text
2
3
2 4 6
0 7 3
7 0 5
3 5 0
```

Facility IDs are zero-based. Instance IDs retain the filename without its `.txt`
extension, including prefixes such as `Am`. Flows should be symmetric; the
importer warns and uses the upper triangle when they are not.

The text solution exporter writes one line per row, containing facility IDs in
layout order. Coordinates follow directly from the widths and this order.

## Published reference

The reference for the space-free multi-row model is the BVNS described by Alberto
Herrán, J. Manuel Colmenar and Abraham Duarte in
[An efficient variable neighborhood search for the Space-Free Multi-Row Facility Layout problem](https://doi.org/10.1016/j.ejor.2021.03.027).
That published implementation is not included in this Java module. The related
[autoconfig study repository](https://github.com/rmartinsanta/ac-SFMRFLP) contains
the separate SF-MRFLP research artifacts; its C build instructions do not apply to
this Maven module.

```bibtex
@article{herran2021efficient,
  title={An efficient variable neighborhood search for the space-free multi-row facility layout problem},
  author={Herr{\'a}n, Alberto and Colmenar, J Manuel and Duarte, Abraham},
  journal={European Journal of Operational Research},
  volume={295},
  number={3},
  pages={893--907},
  year={2021},
  doi={10.1016/j.ejor.2021.03.027}
}
```

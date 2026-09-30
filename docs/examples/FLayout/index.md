---
title: Space-Free Facility Layout
description: The space-free facility layout example in Mork
icon: fontawesome/solid/table-cells
---
# Space-free facility layout

The `example-flayouts` module assigns facilities to ordered rows without gaps.
Each row starts at coordinate zero, and the objective is the flow-weighted sum of
distances between facility centers.

The current importer accepts two-row instances. The default dataset is
`example-flayouts/instances/2rows/`, and `SpaceFreeLayoutExperiment` combines random
construction with best-improvement relocation search. This experiment is a
runnable example; it does not reproduce the published BVNS reference algorithm.

See the [module README](https://github.com/mork-optimization/mork/tree/master/example-flayouts)
for the instance format, build instructions and reference publication.

Both `FLPRandomConstructive` and `FLPRandomConstructiveNew` can be selected by
autoconfig. The module also supplies GRASP candidate managers, other construction
policies, preserving neighborhoods and removal operators. Its exploration filter
limits improver composition to keep the generated parameter space bounded.

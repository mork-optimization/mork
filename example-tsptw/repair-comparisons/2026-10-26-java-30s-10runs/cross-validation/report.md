# Reciprocal C / Java feasibility validation

The original 600 saved tours are no longer available. This report covers a replacement cohort: ten tours from each of the two C implementations and four archived Java variants. Original Java tours come from the 30-second quick experiment; other variants use ten seconds. Seeds are 1234–1243. These records do not replace the earlier performance measurements.

Each imported tour is evaluated by the actual original/fixed C `Solution` implementation and by each archived Java solution model and validator. The Java bug variant is instantiated as its own subclass. There are six evaluators per tour. The C repair patch and all native build commands are saved under `build/` and `build-commands.json`.

| Tour origin | Tours | Cross-language counterpart evaluations | Agreements |
|---|---:|---:|---:|
| Original C | 10 | 40, across four Java evaluators | 40 |
| Fixed C | 10 | 40, across four Java evaluators | 40 |
| Original Java, 30 seconds | 10 | 20, across two C evaluators | 20 |
| Java with C repair mistake | 10 | 20 | 20 |
| Java with long arithmetic | 10 | 20 | 20 |
| Java with arrays and reusable buffers | 10 | 20 | 20 |

All 60 solver tours are feasible. All 360 tour/evaluator combinations agree exactly on travel cost, number of violated windows, total lateness, and final arrival time. Java arrival arrays also match independent recomputation. C checks its internal caches with `Solution::check_solution()`. Of these evaluations, 160 cross the C/Java language boundary.

Controls add 100 reproducible random permutations on the benchmark instance, plus 15 small-instance cases covering arrival exactly at the deadline, waiting, depot-return lateness, and malformed tours. Malformed tours are rejected by the probe import guards before constructing a solution. Valid permutations exercise the actual evaluation and validation code. The total is 1,050 evaluations, with zero discrepancies against an independent Python evaluation.

Agreement on these cases does not prove equivalence for all instances, but it provides no evidence that differing feasibility interpretations explain the observed quality gap. Raw evaluator outputs, input tours, control instances, and the complete mismatch list are retained in this directory.

# Java time-budget quick check

Instance: `n80w200.001.txt`; seeds 1234–1243; ten runs per budget. The original archived Java executable was reused without rebuilding. Objective arithmetic is double, tour storage is the original list, and repair classification is corrected. Metrics were explicitly disabled.

| Java wall-time budget | Runs | Mean cost | Cost-490 hits | Best / worst |
|---|---:|---:|---:|---:|
| 10 seconds, recorded baseline | 10 | 491.80 | 1/10 | 490 / 492 |
| 30 seconds, new runs | 10 | 491.00 | 5/10 | 490 / 492 |

Matched seeds: 4 wins, 6 ties, 0 losses for 30 seconds. Mean candidate-minus-baseline cost difference: -0.80.

The four improved seeds were 1236, 1237, 1238, and 1242. Their recorded time-to-best values were 24.34, 13.66, 14.66, and 21.28 seconds, respectively. All four are beyond the former 10-second cutoff. Extra runtime therefore helps these runs; this small sample does not establish that runtime alone explains the entire C–Java quality gap.

The measured budget averaged 30.000067 seconds of wall time and 29.178 seconds of current-thread CPU time. JVM flags were `-Xms256m -Xmx1g`. Five unmeasured one-second warm-up runs preceded measurement. Their seeds were 1244–1248; the earlier 100-run baseline used 1334–1338. Each measured search resets its RNG to the recorded seed. No other solver batch was launched until all ten 30-second searches finished.

The earlier per-run JSON/tour records are no longer present, so baseline costs were recovered from the retained Java invocation log. The original performance report is not overwritten. New tours, timings, executable and instance hashes, invocation log, and paired results are retained here. The ten new tours passed the archived Java validator and an independent Python recomputation.

The separate `cross-validation/` directory contains replacement C and Java samples and reciprocal evaluators. This is a fresh validation cohort, not a recovery or revalidation of the original 600 missing tours.

Reciprocal validation finished: all ten new 30-second Java tours are accepted by both C evaluators, with matching costs and feasibility values. The separate replacement cohort covers all six variants: 60 tours, 160 cross-language checks, and 1,050 total evaluations including controls, with zero discrepancies. See [cross-validation report](cross-validation/report.md).

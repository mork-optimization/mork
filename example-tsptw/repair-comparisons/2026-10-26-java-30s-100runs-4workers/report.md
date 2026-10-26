# Java 30-second comparison: 100 runs with four parallel workers

Instance: `n80w200.001.txt`; seeds 1234–1333.
The archived original Java executable was reused without rebuilding. Metrics are disabled; arithmetic is double, repair is corrected, and tours use the original ArrayList storage.

| Java wall-time budget | Runs | Mean cost | Cost-490 hits |
|---|---:|---:|---:|
| 10 seconds, recorded baseline | 100 | 491.50 | 25/100 |
| 30 seconds, four workers | 100 | 491.02 | 48/100 |

Matched seeds: 25 wins, 75 ties, 0 losses. Mean candidate-minus-baseline difference: -0.48; paired bootstrap 95% interval: [-0.65, -0.32]; two-sided sign-test p-value: 5.96046e-08.

Mean measured wall time: 30.000098 seconds. Mean current-thread CPU time: 28.776 seconds; range 24.256–29.782. Total experiment elapsed time: 756.7 seconds.

Four JVMs each execute 25 single-threaded searches sequentially. This limits simultaneous searches to four; the operating system schedules them without CPU affinity pinning. JVM flags remain -Xms256m -Xmx1g. Each JVM performs five unmeasured one-second warm-ups, with exact seeds recorded in manifest.json. Warm-up seeds and the multi-JVM execution context differ from the earlier one-JVM baseline. Other processes can affect wall-time budgets; measured current-thread CPU time is retained for each run.

Each saved tour passed the archived Java validator and an independent Python recomputation of cost, permutation completeness, time windows, depot return, and lateness. The earlier tour files are unavailable, so the baseline comparison uses costs recovered from its retained invocation log. Earlier experiments and their results were not overwritten.

Historical comparison, retaining the six earlier 100-run rows:

| Implementation | Budget | Mean cost | Cost-490 hits |
|---|---:|---:|---:|
| Original C | 10 s CPU | 490.45 | 77/100 |
| Fixed C | 10 s CPU | 490.72 | 62/100 |
| Original Java | 10 s wall | 491.50 | 25/100 |
| Java with C repair mistake | 10 s wall | 491.52 | 24/100 |
| Java with long arithmetic | 10 s wall | 491.48 | 26/100 |
| Java with arrays and reusable buffers | 10 s wall | 491.50 | 25/100 |
| Original Java, 30 seconds | 30 s wall | 491.02 | 48/100 |

Extra time improves Java, but the original C reference remains better: mean cost 490.45 and 77/100 hits at ten seconds of process CPU time. The mean-cost gap shrinks from 1.05 to 0.57. Runtime is consistent with part of the explanation; this experiment does not isolate pure throughput from all runtime and algorithmic differences.

All 25 improved runs recorded their best tour after the former ten-second cutoff. Exactly 2 new tours cost 491; the remaining costs are 490 or 492.

The first three historical arms ran concurrently, and later historical variants were appended alone. The new arm uses four parallel JVMs as requested. The previous ten-run 30-second pilot remains separate and is not pooled into these 100 runs.

Native cross-validation: all 100 new tours were independently recomputed and accepted by both original and fixed C evaluators. All 200 native evaluations agree on feasibility, travel cost, violated-window count, total lateness, and final arrival time. C internal cache checks also pass. Raw native outputs and executable hashes are in native-validation/.

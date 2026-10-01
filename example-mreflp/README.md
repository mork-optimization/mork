# Multiple Row Equal Facility Layout (MREFLP)

This Mork example implements the LMLS algorithm in Sun et al., *Learning-driven multi-start local search for the multiple row equal facility layout problem*, [doi:10.1016/j.ejor.2026.09.026](https://doi.org/10.1016/j.ejor.2026.09.026). The supplied paper, raw instances, and workbook remain unchanged. The executable prepares cases, solves them through Mork, exports atomic checkpoints, and independently validates assignments before comparing them with the published results.

## Build and quick validation

Java 25 and Maven are required to build. The campaign runner additionally requires Python 3 on Linux or macOS; it uses only the standard library.

From the repository root:

~~~sh
mvn -pl example-mreflp -am '-Dtest=MREFLP*Test' -Dsurefire.failIfNoSpecifiedTests=false package
cd example-mreflp
java -jar target/mreflp-0.23-SNAPSHOT.jar prepare
python3 -m unittest discover -s scripts -p 'test_*.py'
python3 scripts/campaign.py run --pilot --seconds 1 --warmup-millis 100 --output runs/pilot
python3 scripts/campaign.py report --output runs/pilot-report runs/pilot
~~~

The pilot runs one instance per category for each seed. Short pilot runs validate the workflow; they do not establish reproduction of the 600-second results. For a four-run smoke test add --cohorts normal. The runner labels pilots as pilot-normal / pilot-relaxed.

Preparation produces 572 capacity-specific descriptors (143 sources × capacities 2–5), index files, case/reference JSON manifests, source hashes, and benchmark/audit.md. Each descriptor contains a module-relative raw source path and a capacity. Run the JAR from this module directory, or let the Python runner set its working directory.

## Full campaign on external hardware

For a bounded 7–8 hour validation on this six-performance-core M2 Pro, see [the overnight schedule and commands](overnight-validation.md). The launcher has a 7 h 45 min solving cutoff and a 7 h 50 min overall watchdog.

Package the executable, supplied artifacts, prepared cases, scripts, documentation, and revision/hash manifest:

~~~sh
python3 scripts/campaign.py bundle --output target/mreflp-distribution.tar.gz
~~~

Transfer that archive to the benchmark machine. With Java 25 and Python 3 installed:

~~~sh
tar -xzf mreflp-distribution.tar.gz
cd example-mreflp
python3 scripts/campaign.py run --output runs/paper
python3 scripts/campaign.py report --output runs/paper-report runs/paper
~~~

The full campaign has 6292 runs: all 572 cases with seed 1234 for the normal protocol and seeds 1235–1244 for the ten-run relaxed protocol. Each run gets 600 seconds with no restart cap. Five one-second JVM warm-up runs precede each batch and are outside measured results. One JVM executes one measured run at a time. Total nominal search time is about 1049 CPU hours.

Interrupting a batch retains every completed checkpoint. Rerun the identical command to resume: the runner checks the executable hash, source hashes, configuration, assignments, objective, and timing of existing checkpoints and skips valid runs. Invalid checkpoints are quarantined and rerun; failures and invocation logs remain available. Changing the executable, sources, case selection, or budget requires a new output directory. A directory lock prevents simultaneous writers.

For multiple allocated CPUs, launch separate processes with disjoint shards and separate output directories. Each process remains sequential; launch at most one process per allocated CPU. For example, use --shards 8 --shard-index 0 through --shard-index 7, with output directories runs/shard-0 through runs/shard-7. Combine the directories into one report:

~~~sh
python3 scripts/campaign.py report --output runs/paper-report runs/shard-0 runs/shard-1 runs/shard-2 runs/shard-3 runs/shard-4 runs/shard-5 runs/shard-6 runs/shard-7
~~~

Merge reporting rejects duplicate run identities rather than choosing among repeated results. Retain campaign-manifest.json, failures.jsonl, and logs when transferring results. They record launch settings, hardware, JVM, source revision, and failure history.

## Algorithm and model

Facilities occupy ordered groups; each group holds at most r facilities. The cost is the sum over unordered facility pairs of symmetric flow × absolute group-index distance. Groups and facilities use zero-based indices internally. Flows, objective values, and deltas use 64-bit integers. There is no ordering of facilities within a group.

The implementation includes Algorithms 1–5 and Equations 8–16:

- Multi-start LMLS with a fresh learning matrix for every independent run.
- Learning-driven construction with random facility order, epsilon-greedy selection among nonfull groups, and seeded uniform tie resolution.
- Best admissible OneMove tabu search, facility tabu expiration, aspiration against the phase incumbent, and a depth of 20 consecutive iterations without improvement. The best encountered solution is returned, even after worsening moves.
- Best-improvement atomic swaps, accepted only for strictly negative deltas.
- Direct relocation/swap evaluators and incremental caches, including post-move updates.
- Reward, penalty, compensation, and a single literal smoothing pass without additional row normalization.

| Parameter | Value |
|---|---:|
| Greediness epsilon | 0.6 |
| Tabu non-improvement depth | 20 |
| Uniform integer tabu tenure | 1–3 |
| Reward alpha | 0.1 |
| Penalty beta | 0.2 |
| Compensation gamma | 0.3 |
| Smoothing rho | 0.3 |

The six ablations are RANDOM (LMLS1), GREEDY (LMLS2), WITHOUT_TABU, WITHOUT_SWAP, DIRECT_ONE_MOVE (LMLS3), and DIRECT_SWAP (LMLS4). PaperExperiment runs one configured variant; AblationExperiment runs all seven. For a local ablation smoke test:

~~~sh
java -jar target/mreflp-0.23-SNAPSHOT.jar --solver.experiments=AblationExperiment --instances.path.default=benchmark/pilot.index --mreflp.time-limit-seconds=1 --solver.warmup.max-millis=100 --mreflp.protocol=ablation-pilot --serializers.mreflp.folder=runs/ablations
java -jar target/mreflp-0.23-SNAPSHOT.jar report runs/ablation-report runs/ablations
~~~

To select one ablation, pass --mreflp.variant=GREEDY (or another enum above). For deterministic iteration-based debugging, set --mreflp.max-restarts=25; time control still takes precedence. This is not a paper campaign. Published ILP, SDP, AMA2, and GRASP values are reference providers; their external solvers are not part of this LMLS reimplementation. Full ablation experiments, irace calibration, and statistical significance studies are outside this initial reproduction.

### Explicit conventions

The paper adopts a group-count upper bound but does not supply the value of k for every case. This example uses Theorem 3 and Table 1 of [Anjos et al. (2018), Improved Exact Approaches for Row Layout Problems with Departments of Equal Length](https://www.research.ed.ac.uk/files/85925183/Improved_Exact_Approaches_for_Row_Layout_Problems_with_Departments_of_Equal_Length.pdf). For n ≤ 16 and r = 2, 3, 4 the exact table is used. Otherwise:

- k = 1 when n ≤ r; k = 2 when r < n < 1.5r + 1.5.
- For r = 2 and n ≥ 9: k = ceil(2n/3) − 1.
- For odd r: k = floor(2n/(r+1)).
- For even r = 4: k = 2 ceil((n−r/2−1)/(r+1)) + 1.

The general even-r bound may be conservative. Derived k values are recorded in cases.json and Mork instance properties. This convention is material when interpreting objective differences.

Other details absent from the pseudocode are fixed explicitly: uniform seeded tie resolution; random facility order and minimum marginal-cost group assignment for the greedy ablation; tabu expiration at the incremented iteration plus a random integer in 1–3; idle iterations if every feasible relocation is tabu; termination of the tabu phase if no relocation is feasible. The chosen generator is Mork's Xoroshiro128PlusPlus, with seed reset for every work unit. The authors' LMLS source code is not supplied, so seed equality does not imply identical stochastic trajectories. Construction finishes a feasible assignment when a very short deadline expires.

## Reference audit and reporting

The workbook reader consumes only each sheet's primary top-left table and retains cell provenance. The supplementary readme maps sheets to capacities and identifies time columns as **time to reach the best result**, not total runtime.

- All four N-15 capacity cases exist as raw data but are omitted from the workbook: 572 generated cases versus 568 published LMLS references.
- Nineteen QAP_sko files have five missing unit-width entries. Only these recognizable shortened headers are accepted; full matrix dimensions and values are validated.
- Upper triangular matrices are mirrored; already symmetric matrices are counted once. Duplicate sko replicate matrices retain their separate case names.
- Five normal LMLS entries in Table A.9, G33:G37, report 109054 while the relaxed best entries report 189054. These values remain unchanged and are flagged.
- GRASP Table A.12 E36 reports the fractional objective 75542.100000000006. It remains unchanged and is flagged.
- There are 289 BKV entries marked optimal. Optimality flags are kept separately from LMLS comparison scores.

Every .run.json checkpoint stores the assignment, integral cost, runtime and time to best in nanoseconds, seed, parameters, protocol, source/executable hashes, JVM/OS details, and warm-up settings. Temporary files are renamed atomically after successful validation. Reporting reloads the raw instances and recomputes each assignment's pairwise cost independently of incremental caches.

Report outputs are report.md, comparisons.csv, coverage.csv, and status.json. Comparisons include best cost, mean relaxed cost, runtime, mean time to best, published values, absolute/percentage gaps, source cells, and audited win/tie/loss counts by category/capacity. Gaps are result minus reference; negative gaps are wins. Percentage gaps with a zero reference are undefined and left empty. Flagged references remain visible but are excluded from audited counts. Partial relaxed cohorts are excluded from those counts.

A report is a complete paper campaign only when every expected seed/case has a valid 600-second LMLS checkpoint with paper parameters, the specified generator and warm-up, one executable version, and no protocol warnings. Partial pilots never become a complete campaign. The full comparison remains pending until the external campaign finishes.

**Manual review gate:** inspect the full report, coverage, source anomalies, feasibility, and timing evidence before making further algorithm changes. This workflow performs no tuning or automatic modification in response to result gaps. Matching stochastic results is an empirical outcome, not an implementation guarantee.

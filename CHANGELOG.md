# Developing
- (New) StringUtil reverse, longestCommonPrefix, longestCommonSuffix
- (New) Remove lcp and lcs from instance names
- (New) Autoconfig: Add Enum types compatibility
- (New) BitSet implementation compatible with JDK APIs
- (Breaking change) maximize as a boolean has been changed in all algorithm components to FMode.MINIMIZE and FMode.MAXIMIZE
- (Fix) #107 Autoconfig: Cannot assign integer values to double parameters in algorithm components

# v0.16
- (New) Generic ScatterSearch algorithm implementation.
- (New) More methods in StringUtil + missing tests.
- (New) Custom solution properties.
- (New) Default readme in generated projects
- (New) Validate that no solution improves optimal value if known in any ReferenceResultProvider
- (Change) Improve progress bar and logging
- (Change) Upgrade dependencies
- (Change) Improve handling of missing ReferenceValues when comparing results
- (Fix) Max line length in InstanceManager
- (Fix) Multiple improvements to autoconfig module

# v0.15.1
- (Change) Remove autoconfig enable/disable from config files, use --autoconfig param
- (Fix) Autoconfig bugfix: more robust @CategorialParam serializing for irace
- (Fix) Autoconfig bugfix: recursive generation context when children is empty
- (Fix) Autoconfig bugfix: automatically remove single quotes from parameter values before running parser
- (New) API endpoint /autoconfig/debug/slow returns slow executions that did not respect the time limit
- (Change) Rename some autoconfig related method and classes, simplify their API

# v0.15
- (New) Metrics class to keep track of different metrics while any algorithm is executing. Calculate hypervolume of any metric in given range.
- (New) ArrayUtil::max, ArrayUtil::min util methods for int, long and double primitives
- (New) DoubleComparator util functions
- (New) Warn on slow solution serialization (> 500ms)
- (New) Add option to choose how frequently any solution serializer should execute: best_per_instance, best_per_alg_instance and all iterations.
- (New) Solving progress bar, logging improvement
- (New) Add solution exporter to template project
- (New) Profile activation via --{profilename}, example: --irace --autoconfig
- (Change) All irace related classes moved to autoconfig module
- (Change) Refactored GRASP Algorithm: Use builder pattern, add option to provide a custom greedy function.
- (Change) Refactored LocalSearch, enhance and simplify its behaviour.
- (Change) Improve SolutionSerializer::export method
- (Change) Solution::updateLastModifiedTime renamed to Solution::notifyUpdate
- (Change) Split configuration in different files
- (Removed) IteratedImprover, MoveComparator, DefaultMoveComparator, Move::improves
- (Fix) Irace bug when parsing args separated by multiple spaces

# v0.14
- (New) Algorithms can be magically built from strings without user interaction, using the new autoconfig project. See AlgComponentService and AlgorithmBuilderUtil classes for more details.
- (New) Algorithms can be manually built from strings using an IraceAlgorithmGenerator implementation. See IraceAlgorithmGenerator::buildFromString for more details
- (New) Benchmark enabled by default, using cached results file.
- (New) Add Other info Excel sheet
- (New) ReferenceResult::isOptimalValue
- (New) Default Instance::toString
- (New) Measure instance load time, add to Excel as a instance property by default
- (Change) Template project: Add base Move, ReferenceResultProvider, better comments
- (Change) dynamically generate scenario.txt for Irace. Use seed, instances, parallel config etc loaded from application.yml or environment.
- (Change) Neighborhoods refactor: delete eager and lazy neighborhoods, randomizable as subclass, always return ExploreResult.
- (Change) IraceAlgorithmGenerator refactor and cleanup, improve validation.
- (Change) Move::execute now must return boolean, stating if current solution has changed after executing it or not. There are moves that may leave the solution exactly the same in certain problems.
- (Change) Upgrade dependencies
- (Remove) Move::isValid, not practical in its current implementation.
- (Remove) NoOPConstructive, replaced by Constructive::nul
- (Fix) #88: Filename bug in SolutionSerializer, by @ea2809
- (Fix) #63: Bug parallel irace with randoms, by @scaverod
- (Fix) RawSheetWriter::writeCell add float and long value handling

# v0.13
- (New) Neighborhood.empty(), Neighborhood.concat(n1, n2), Neighborhood.interleave(n1, n2)
- (New) Modular Angular 13 frontend
- (New) Custom instance properties. See Instance::getProperty and setProperty for more info.
- (New) ExecutionStartedEvent::isMaximizing
- (New) Instance preload by default, set solve order by default to instance path, override by implementing Instance::compareTo.
- (New) Root package name can be optionally customized
- (New) TimeUtil class
- (Change) Improved logging, lot less noise
- (Change) Simplify algorithms constructors, create no-op components with Constructive::null, Improver::null, Shake::nul
- (Change) Removed maximizing config parameter, now must be set when calling Mork.start from main method.
- (Change) Remove logging properties from configuration file, mostly unused, but can still be overridden.
- (Change) Remove advanced properties from configuration file, try patch and skip if not possible.
- (Change) Add "common" submodule. More submodules will be added soon.
- (Fix) ExcelSerializer: If a value to write is null then write empty cell instead of failing
- (Fix) #77: Use reference results in all ResultSerializers instead of only when exporting to XLSX.
- (Fix) #31: Frontend mixed experiment data under some circumstances. 
- (Fix) #51: Frontend instance table alignment was wrong if a custom solution was bigger than a chart. 
- (Fix) #40: Frontend displayed wrong data for maximization problems. 

# v0.12
- (New) GRASP::afterGRASP: Optionally execute anything after the GRASP constructive finishes
- (New) Verify move changes solution score as expected
- (New) Option to enable/disable instance preload
- (New) Warn if IteratedImproved appears stuck
- (New) Added SolutionValidator to template project by default
- (Refactor) Results Serializers: CSV, XLSX
- (Fix) Workload hard limit 1mk
- (Fix) Benchmark should use seed configured via properties
- (Fix) Missing return in SimulatedAnnealingBuilder method
- (Fix) Export all generated solutions by default if a solution serializer is enabled instead of only best for pair (alg, instance)



# v0.11
- (New) Swap methods in ArrayUtil
- (New) Events: AlgorithmProcessingStartedEvent, AlgorithmProcessingEndedEvent
- (Improvement) AbstractExperiment now requires SolverConfig instead of a "strange" boolean value.
- (Improvement) IteratedGreedy
- (Improvement) SimulatedAnnealing: Customizable Acceptance Criteria
- (Improvement) Irace: return both objetive function and elapsed time.
- (Fix) Run configurations in template project
- (Fix) Disable websockets logging by default
- (Fix) Excel logger gave the impression to be stuck
- (Fix) Potential classpath conflict with JSONObject
- (Docs) Events and result export / solution serialization

# v0.10
Mainly internal changes, refactored some critical components
- (New) Refactor Executors: ConcurrentExecutor can be as much as 8 times faster for certain workloads.
- (New) Refactor solutions serializers: Easily extend and customize how solutions should be exported to disk. Disable or keep default JSON serializer.
- (New) Refactor results serializers: New features, the most prominent one now they can export after solving each instance to provide instant feedback instead of waiting until the whole experiment ends.
- (New) Added TSP example and documentation.
- (New) Detect in some cases when user does not update TTB and throw exception.
- (Fix) Added missing irace file during project generation via web.
- (Fix) Irace logs were not included in log files.
- (Fix) Irace middleware.sh now works properly in Mac.
- (Fix) Max length for algorithms short names, throws exception before experiment starts if any algorithm is longer.

# v0.9
- (Fix) PatchMathRandom and PatchCollections for Java 11 and newer.
- (New) MultiStartAlgorithm
- (New) Simulated Annealing
- (New) Customizable random implementations. RandomManager::getRandom now returns an instance of RandomGenerator.
- (Deprecated) SimpleMultiStartAlgorithm
- Java 17 as minimum required version
- Replaced Solution::getBetterSolution with Solution::isBetterThan
- Better generic usage in Solution --> Solution<Self,Instance>
- (NEW) Easily create Docker containers

# v0.8
- Internal Maven changes

# v0.7
- Changed ReferenceResultProvider, now multiple instances can be provided in order to automatically compare against several approaches.
- Improved Excel serializer: Configurable metrics in pivot table, better generation in raw table. All calculated values are not generated by Excel.
- Fixed several bugs and other minor improvements

# v0.6
- Irace integration, see Wiki for more details.
- Algorithm::getBuilder: get current SolutionBuilder.
- Two types of neighborhoods, normal and lazy. Lazy generated movements under demand, while eager returns a collection of movements.
- New event type: ErrorEvent, when an unhandled exception is propagated inside an Executor.
- Telegram bot, if enabled, sends message onErrorEvent.

# v0.5
- Added Telegram integration, see Wiki for more information and how to use.
- Fixed bug in IteratedGreedy
- Added option to allow users to decide if the backend should stop and the application be killed after all experiments are finished.


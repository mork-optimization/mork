# Component timing traces

Enable detailed per-call timings with
`--solver.time-stats=true`. Works with sequential or parallel experiments. The default is `false`. Total execution time, time-to-best,
and objective curves do not require this setting.

TimeStats records entry
and exit timestamps for the default method of algorithm, constructive, improver, and shake classes. 
Any other method can be observed by annotating it with `@TimeStats`. Durations
are inclusive: a parent's duration includes nested calls. Exceptional exits are
recorded too. Warm-up and autoconfig executions do not generate timing data. Manually creating threads do not inherit de timings scope. 

Example configuration:
```yaml
solver:
  time-stats: true
time-stats:
  output-directory: time-stats
  batch-size: 4096
  queue-capacity: 64
  pool-capacity: 64
  flush-interval: 1s
  shutdown-timeout: 5s
```

Each application run gets a unique directory. While running, the TimeStats writer
appends to `events.csv.partial` and `executions.csv.partial`. After a successful
flush and close, these become `events.csv` and `executions.csv`. The final
`manifest.json` is published last. A completed file may still contain an
**incomplete trace** if batches were dropped: always inspect the manifest first.

File structure is as follows:

| File             | Contents                                                                                      |
|------------------|-----------------------------------------------------------------------------------------------|
| `events.csv`     | `run_id,execution_id,thread_id,component_class,method_signature,start_ns,end_ns`              |
| `executions.csv` | `run_id,execution_id,experiment,instance_path,instance_id,algorithm,repetition,seed`          |
| `manifest.json`  | Schema version, clock origin, recording status, observed/dropped/flushed counts, error if any |

Execution IDs match `WorkUnitResult.resultId`, including failed executions. CSV
is UTF-8 with quoted commas, quotes, and line breaks. Nanosecond timestamps are
relative to the run's monotonic clock; subtract `start_ns` from `end_ns`
for duration. Rows are in batch arrival order, they may not be sorted by timestamp.
Nested invocations normally appear before their enclosing invocation. Concatenate
files of the same type by keeping one header and appending the remaining rows;
IDs remain unique across runs. Join events to executions using their IDs.

Full batches are submitted immediately. Partial batches are submitted on the
next recorded call after the flush interval, or at execution end. Idle partial
batches stay bounded until then. The writer independently flushes accepted data
at the interval even when producers are idle. 

If the writing queue is full, the producer may drop a batch of events and continue. Periodic
warnings and the final manifest identify if any data loss has occurred. Solver thread do NOT  wait for
disk I/O, if the disk is too slow, or events are generated too fast, they are dropped. 
A write failure disables recording; algorithm execution and validation
continue. Failed output retains temporary filenames. Shutdown waits at most the
configured timeout, then reports incomplete output and leaves the writer as a
daemon if the filesystem does not respond. Abrupt termination can leave partial
files without a manifest and a truncated final CSV row. Do not treat them as a
complete recording. 

`observedEvents` counts events submitted from producer buffers. `droppedEvents`
counts known discarded events. `flushedEvents` acknowledges only successful
writer flushes; after an I/O failure, additional rows may exist beyond that
acknowledged prefix. A failed or interrupted recording does not claim exact
persisted counts. `COMPLETE` means normal finalization with no dropped events;
`INCOMPLETE` denotes known omissions; `FAILED` denotes a writer/shutdown error.

Raw CSV volume grows with captured calls. Recording and writing also consume CPU
and disk bandwidth: compare overhead and loss counts before using traces for
performance conclusions. Increasing queue capacity can absorb temporary bursts;
it cannot fix sustained input faster than the writer.

TimeStats can be analyzed by using the Python script: TODO complete doc.
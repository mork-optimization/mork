#!/usr/bin/env bash

java -jar target/tsptw-0.23-SNAPSHOT.jar \
  --solver.experiments=FinalExperiment \
  --solver.repetitions=30 \
  --solver.parallelExecutor=true \
  --solver.nWorkers=30 \
  --instances.path.default=instances/ \
  --solver.metrics=true \
  --serializers.excel.enabled=true

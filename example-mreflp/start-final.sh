#!/usr/bin/env bash

java -jar target/mreflp-0.23-SNAPSHOT.jar \
  --solver.experiments=FinalExperiment \
  --blacklist=MREFLPFocusedInventoryFilterNew \
  --solver.repetitions=30 \
  --solver.parallelExecutor=true \
  --solver.nWorkers=30 \
  --instances.path.default=instances/all.index \
  --mreflp.time-limit-seconds=60 \
  --mreflp.protocol=validation \
  --solver.metrics=true \
  --serializers.excel.enabled=true \
  --serializers.mreflp.folder=runs/elites-validation

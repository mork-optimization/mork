#!/usr/bin/env bash
set -euo pipefail

cd -- "$(dirname -- "${BASH_SOURCE[0]}")"

exec java -jar target/bmssc-0.23-SNAPSHOT.jar \
  --autoconfig --blacklist=BlacklistedComponents "$@"

#!/usr/bin/env bash
set -euo pipefail

cd -- "$(dirname -- "${BASH_SOURCE[0]}")"

# TSPTWExplorationFilter is applied automatically; this project has no blacklist class.
exec java -jar target/tsptw-0.23-SNAPSHOT.jar --autoconfig "$@"

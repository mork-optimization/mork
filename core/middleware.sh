#!/bin/bash
###############################################################################
# This script is the command that is executed every run.
# Check the examples in examples/
#
# This script is run in the execution directory (execDir, --exec-dir).
#
# PARAMETERS:
# $1 is the candidate configuration number
# $2 is the instance ID
# $3 is the seed
# $4 is the instance name
# The rest ($* after `shift 4') are parameters to the run
#
# RETURN VALUE:
# This script should print one numerical value: the cost that must be minimized.
# Exit with 0 if no error, with 1 in case of error
###############################################################################

HOST=localhost
PORT=8080
CONFIG_PARAMS=$(echo -ne "$*" | base64 | tr --delete "\n")

curl -X POST --data "key=__INTEGRATION_KEY__&config=${CONFIG_PARAMS}" "http://${HOST}:${PORT}/api/execute"



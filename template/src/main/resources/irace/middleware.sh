#!/bin/bash

HOST=localhost
PORT=8080
CONFIG_PARAMS=$(echo -ne "$*" | base64 | tr -d " \n")

# Request an algorithm execution to the Mork Execution Controller via its REST API
curl -s -X POST -H 'Content-Type: application/json' --data "{\"key\":\"__INTEGRATION_KEY__\",\"config\":\"${CONFIG_PARAMS}\"}" "http://${HOST}:${PORT}/execute"



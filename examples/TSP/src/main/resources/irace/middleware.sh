#!/bin/bash

HOST=localhost
PORT=8080
CONFIG_PARAMS=$(echo -ne "$*" | base64 | tr -d " \n")

curl -s -X POST -H 'Content-Type: application/json' --data "{\"key\":\"__INTEGRATION_KEY__\",\"config\":\"${CONFIG_PARAMS}\"}" "http://${HOST}:${PORT}/execute"



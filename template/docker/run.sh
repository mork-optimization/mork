#!/bin/sh
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 username/__RNAME__"
    exit 1
fi

mkdir -p results
docker run -t -p8080:8080 -v "$(pwd)"/results:/results "$1:latest"

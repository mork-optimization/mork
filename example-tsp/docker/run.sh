#!/bin/sh
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 username/projectname"
    exit 1

fi

cd "$(dirname "$0")/../" || { echo "Invalid project structure"; exit 1; }
mkdir -p results
docker run -t -p8080:8080 -v "$(pwd)"/results:/results "$1:latest"

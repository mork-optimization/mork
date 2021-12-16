#!/bin/sh
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 username/projectname"
    exit 1
fi

cd "$(dirname "$0")/../" || { echo "Invalid project structure"; exit 1; }
mvn clean package -DskipTests
docker build -t "$1:latest" -f docker/Dockerfile .

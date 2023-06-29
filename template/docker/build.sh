#!/bin/sh
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 username/__RNAME__"
    exit 1
fi

folder=${PWD##*/}
if [ "$folder" = "docker" ]; then
    cd ..
fi

docker build -t "$1:latest" -f docker/Dockerfile .

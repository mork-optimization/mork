#!/bin/sh
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 username/__RNAME__"
    exit 1
fi

docker login
docker push "$1:latest"

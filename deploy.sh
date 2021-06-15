#!/bin/bash -e
cd core
mvn clean deploy
cd ../parent
mvn clean deploy
cd ../template
mvn clean deploy


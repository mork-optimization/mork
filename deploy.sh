#!/bin/bash -e

# Deploy artifacts to Maven Central
cd core
mvn clean deploy
cd ../parent
mvn clean deploy
cd ../template
mvn clean deploy


#!/bin/bash -e

# Install artifacts in local repository
cd core
mvn clean install -Dgpg.skip=true
cd ../parent
mvn clean install -Dgpg.skip=true
cd ../template
mvn clean install -Dgpg.skip=true



#!/bin/bash -e

# Install artifacts in local repository
cd core
mvn clean install
cd ../parent
mvn clean install
cd ../template
mvn clean install



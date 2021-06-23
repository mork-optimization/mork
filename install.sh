#!/bin/bash -e
cd core
mvn clean install
cd ../parent
mvn clean install
cd ../template
mvn clean install



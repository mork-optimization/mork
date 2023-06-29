#!/bin/bash -e

mvn clean install -Dgpg.skip=true -DskipTests

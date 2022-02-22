#!/bin/bash -e
#mvn clean install

mvn -Dgpg.useagent=false -B release:clean release:prepare release:perform

#!/bin/bash -e
#mvn clean install

mvn -B release:clean release:prepare release:perform

#!/bin/bash -e

mvn -B release:clean release:prepare release:perform

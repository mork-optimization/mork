#!/bin/sh

mvn -Dgpg.skip -B jacoco:prepare-agent verify

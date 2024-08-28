#!/bin/sh
set -e

mvn -Dgpg.skip -B clean verify

path="aggregate-report/target/site/jacoco-aggregate/jacoco.xml"
if command -v open > /dev/null 2>&1
then
    open "$path"
    exit
fi
if command -v xdg-open > /dev/null 2>&1
then
    xdg-open "$path"
    exit
else
    echo "Results available at '$path'"
fi

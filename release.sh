#!/bin/bash -e
#mvn clean install

# Check if gpg signing is correctly configured
echo "" | gpg --sign >/dev/null

# Do release
mvn -B release:clean release:prepare release:perform

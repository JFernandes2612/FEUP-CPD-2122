#!/usr/bin/env bash

# Simple bash script to be run at the start of the project, in /src/main/java
# The output will show up on the /build folder, next to the /src one
#
# Be sure to chmod +x
#
# TO RUN IT IN THE PROJECT ROOT, do it as ../scripts/build.sh

javac -d ./build */*.java */*/*.java

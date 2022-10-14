#!/usr/bin/env bash

# Simple bash script to be run after every run of the project, in /src/main/java
# It will completely erase all files from a specific node, or from every single one
#
# Be sure to chmod +x it first
#
# TO RUN IT IN THE PROJECT ROOT (/src) , do it as ../scripts/cleanup.sh
arg=$1
all="all"
if [ "$#" -eq 1 ];
then
    if [ "$1" = "$all" ];
    then
        rm -rf storage/
        rm -rf logs/
        rm -rf membership/
    else
        node_id=$1
        rm -rf storage/${node_id}
        rm -rf logs/${node_id}
        rm -rf membership/${node_id}
    fi
else
    echo "Usage: $0 [<node_id>] | all"
    exit 1
fi

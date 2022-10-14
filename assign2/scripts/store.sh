#!/usr/bin/env bash

# Simple bash script to execute a store
# It should be run inside the build folder
#
# Be sure to chmod +x
#
# TO RUN IT IN THE PROJECT ROOT, do it as ../scripts/store.sh



if [ "$#" -ne 4 ]
then
    echo "Usage: $0 <IP_mcast_addr> <IP_mcast_port> <node_id>  <Store_port>"
    exit 1
fi

# Assign input arguments to nicely named variables

ip_mc=$1
port_mc=$2
id=$3
port=$4

# Execute the program
java store.Store ${ip_mc} ${port_mc} ${id} ${port}

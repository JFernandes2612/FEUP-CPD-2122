#!/usr/bin/env bash

# Simple bash script to execute a store
# It should be run inside the build folder
#
# Be sure to chmod +x
#
# TO RUN IT IN THE PROJECT ROOT, do it as ../scripts/client.sh



if [ "$#" -lt 2 ]
then
    echo "Usage: $0 <node_ap> JOIN|LEAVE|PUT|GET|DELETE [<opnd>]"
    exit 1
fi

# Assign input arguments to nicely named variables

ap=$1
op=$2

case $op in
JOIN)
    if [ "$#" -ne 2 ]
	then
		echo "Usage: $0 <node_ap> JOIN"
		exit 1
	fi
    operand=""
    ;;
LEAVE)
    if [ "$#" -ne 2 ]
	then
		echo "Usage: $0 <node_ap> LEAVE"
		exit 1
	fi
    operand=""
    ;;
PUT)
    if [ "$#" -ne 3 ]
	then
		echo "Usage: $0 <node_ap> PUT <file_path>"
		exit 1
	fi
    operand=$3
    ;;
GET)
    if [ "$#" -ne 3 ]
	then
		echo "Usage: $0 <node_ap> GET <key>"
		exit 1
	fi
    operand=$3
    ;;
DELETE)
    if [ "$#" -ne 3 ]
	then
		echo "Usage: $0 <node_ap> DELETE <key>"
		exit 1
	fi
    operand=$3
    ;;
esac

# Execute the program
java client.TestClient ${ap} ${op} ${operand}

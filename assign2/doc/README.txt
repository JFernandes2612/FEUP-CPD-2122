Note that you should always run these scripts inside the project src folder!
Also, it is necessary to first make them executable by executing:

cd src/

chmod +x ../scripts/*.sh

 Then, in order to compile the project:

sh ../scripts/build.sh

To clean up the files - storage, logs and memberships folders. This should be used before any fresh tests, otherwise data can become senseless:

sh ../scripts/cleanup.sh

In order to run a test client:

sh ../scripts/client.sh <node_ap> <operation> [<opnd>]

Where <node_ap>, <operations> and <opnd> follow the same conventions as in the project Specification

To run a store server:

sh ../scripts/store.sh <IP_mcast_addr> <IP_mcast_port> <node_id>  <Store_port>

Where <IP_mcast_addr>, <IP_mcast_port>, <node_id> and <Store_port> follow the same conventions as in the project Specification

To start the project, there is a run script that will start 3 servers automatically.
Note that the rmiregistry program should be running before running this script.
Also, the script was only used on Ubuntu (hence the use of gnome-terminal):

sh ../run.sh

It is in experimental stages, so it might not be the most effective solution.
Anyways, it is easy to understand what it does, and to replicate it with 3 different terminals.

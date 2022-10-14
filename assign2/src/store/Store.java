package store;

import communication.MulticastDispatcher;
import communication.PeriodicMulticastMessageSender;
import communication.RMI;
import communication.TCPDispatcher;
import communication.messages.DeleteMessage;
import communication.messages.GetMessage;
import communication.messages.JoinMessage;
import communication.messages.LeaveMessage;
import communication.messages.MembershipMessage;
import communication.messages.PutMessage;
import utils.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static utils.Utils.bytesToHexString;
import static utils.Utils.calculateHash;

public class Store implements RMI {
    private final String nodeID;
    private final String nodeHashValue;
    private final int tcpPort;
    private final store.MembershipCounterManager membershipCounterManager;
    private int receivedMembershipMessages;

    private final store.LogManager logManager;

    private final HashMap<String, String> clusterIPs;
    private final HashMap<String, Integer> clusterPorts;
    private List<String> clusterIDs;
    private List<String> clusterHashes;
    private String successorHash;

    private final StorageManager storageManager;

    private MulticastDispatcher multicastDispatcher;
    private TCPDispatcher tcpDispatcher;
    private PeriodicMulticastMessageSender periodicSender;

    public static void main(final String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java Store <IP_mcast_addr> <IP_mcast_port> <node_id>  <Store_port>");
        } else {
            try {
                final Store node = new Store(args);

                node.initDispatchers(args);

                RMI stub = (RMI) UnicastRemoteObject.exportObject(node, 0);

                Registry registry = LocateRegistry.getRegistry();
                registry.bind(args[2], stub);

                System.out.println("The server is ready");

            } catch (final IOException e) {
                System.out.println("Node communication error: " + e.getMessage());
                e.printStackTrace();
            } catch (AlreadyBoundException e1) {
                System.out.println("Error binding ID: " + e1.getMessage());
                e1.printStackTrace();
            }
        }

    }

    public List<String> getClusterIDs() {
        return this.clusterIDs;
    }

    public String getID() {
        return this.nodeID;
    }

    public Store(final String[] args) {

        this.receivedMembershipMessages = 0;
        this.nodeID = args[2];
        this.nodeHashValue = hashItem(this.nodeID);
        this.tcpPort = Integer.parseInt(args[3]);

        this.membershipCounterManager = new MembershipCounterManager(this.nodeID);

        this.clusterIPs = new HashMap<String, String>();
        this.clusterPorts = new HashMap<String, Integer>();
        this.clusterIDs = new ArrayList<String>();
        this.clusterHashes = new ArrayList<String>();

        this.clusterIDs.add(this.nodeID);
        this.clusterHashes.add(this.nodeHashValue);

        this.logManager = new LogManager(this.nodeID);

        this.storageManager = new StorageManager(this.nodeID);
    }

    public void initDispatchers(final String[] args) throws IOException {
        this.multicastDispatcher = new MulticastDispatcher(args[0], Integer.parseInt(args[1]), this);
        final Thread multicastThread = new Thread(this.multicastDispatcher);
        multicastThread.start();

        this.tcpDispatcher = new TCPDispatcher(this.tcpPort, this);
        final Thread tcpThread = new Thread(this.tcpDispatcher);
        tcpThread.start();

        this.periodicSender = new PeriodicMulticastMessageSender(this);
        this.periodicSender.updateMembershipPeriodically();
    }

    @Override
    public String join() throws RemoteException {
        try {
            int res = this.enterCluster();
            if (res == 0) {
                return "Node " + this.nodeID + " has joined the cluster";
            } else {
                return "Node has already joined the cluster";
            }
        } catch (UnknownHostException e) {
            System.out.println("Cannot determine localhost");
            e.printStackTrace();
            return "Error: Cannot determine localhost";
        }
    }

    @Override
    public String leave() throws RemoteException {
        int res = this.leaveCluster();
        if (res == 0) {
            return "Node " + this.nodeID + " has left the cluster";
        } else {
            return "Node has already left the cluster";
        }
    }

    private String hashItem(String value) {
        return bytesToHexString(calculateHash(value));
    }

    private void sortCluster() {
        Collections.sort(this.clusterHashes);
    }

    private void updateSuccessor() {
        switch (this.clusterHashes.size()){
            case 0: // should never happen
                this.successorHash = "";
                break;
            case 1: // cluster with only this node
                this.successorHash = this.nodeHashValue;
                break;
            default:
                int nodeIndex = this.clusterHashes.indexOf(this.nodeHashValue);
                this.successorHash = this.clusterHashes.get((nodeIndex + 1) % this.clusterHashes.size());
                break;
        }
    }

    /**
     * @brief add nodeHash to the local representation of the ring cluster
     */
    public void joinRing(String nodeHash) {
        if (!this.clusterHashes.contains(nodeHash)) {
            this.clusterHashes.add(nodeHash);
            sortCluster();
        }
    }

    /**
     * @brief remove nodeHash from the local representation of the ring cluster
     */
    public void leaveRing(String nodeHash) {
        this.clusterHashes.remove(nodeHash);
        this.successorHash = "";
    }

    public int enterCluster() throws UnknownHostException {

        int counterValue = this.membershipCounterManager.getMembershipCounter();
        if (counterValue % 2 == 0) {
            System.out.println("Node has already joined the cluster");
            return -1;
        }

        joinRing(this.nodeHashValue);
        updateSuccessor();

        this.membershipCounterManager.incrementMembershipCounter();
        int sentJoinMessages = 0;
        final String logMessage = this.nodeID + " JOIN "
                + Integer.toString(membershipCounterManager.getMembershipCounter()) + "\n";
        logManager.writeToLog(logMessage);
        while (sentJoinMessages != 3) {
            sendJoinMessage();
            sentJoinMessages++;
            int timeElapsed = 0;
            final long start = System.currentTimeMillis();
            // Waiting for handleMembership to receive messages
            while (this.receivedMembershipMessages < 3 && timeElapsed < 3) {
                final long timeAfter = System.currentTimeMillis();
                timeElapsed = (int) ((timeAfter - start) / 1000);
            }
        }

        return 0;
    }

    public int leaveCluster() {

        int counterValue = this.membershipCounterManager.getMembershipCounter();

        if (counterValue % 2 == 1) {
            System.out.println("Node has already left the cluster");
            return -1;
        }

        // leave the local cluster
        leaveRing(this.nodeHashValue);

        this.membershipCounterManager.incrementMembershipCounter();
        final String logMessage = this.nodeID + " LEAVE "
                + Integer.toString(membershipCounterManager.getMembershipCounter()) + "\n";
        logManager.writeToLog(logMessage);
        this.clusterHashes.remove(this.nodeHashValue);
        transferToSuccessor();
        sendLeaveMessage();
        this.periodicSender.stopLoop();
        this.tcpDispatcher.stopLoop();
        this.multicastDispatcher.stopLoop();


        return 0;
    }

    public void receiveMembershipMessage(final String senderID, final String members, final String body) {
        if (senderID.equals(this.nodeID)) {
        } else {
            this.receivedMembershipMessages++;
            final List<String> clusterMembers = Arrays.asList(members.split("-"));
            final List<String> clusterMemberHashes = clusterMembers.stream()
                    .map(x -> hashItem(x)).collect(Collectors.toList());

            clusterIDs = Utils.getListUnion(clusterIDs, clusterMembers);
            clusterHashes = Utils.getListUnion(clusterHashes, clusterMemberHashes);
            updateSuccessor();
            this.logManager.writeToLog(body);
        }
    }

    public void receiveJoinMessage(final String senderID, final int membershipCounter, final String senderIP,
                                   final int senderPort) {
        if (senderID.equals(nodeID)) {
        } else {
            if (!clusterIDs.contains(senderID)) {
                // Update internal cluster state
                this.clusterIDs.add(senderID);
                Collections.sort(this.clusterIDs);
                joinRing(hashItem(senderID));
                updateSuccessor();
                this.clusterIPs.put(senderID, senderIP);
                this.clusterPorts.put(senderID, senderPort);

                // Add to log events
                final String logMessage = senderID + " JOIN " + Integer.toString(membershipCounter) + "\n";
                this.logManager.writeToLog(logMessage);
                sendMembershipMessage(senderIP, senderPort);
            }
        }
    }

    public void receiveLeaveMessage(String senderID, int membershipCounter) {
        if (senderID.equals(nodeID)) {

        } else {
            System.out.println("this node is " + this.nodeID);
            System.out.println("sender node is " + senderID);
            final String logMessage = senderID + " LEAVE " + Integer.toString(membershipCounter) + "\n";
            this.logManager.writeToLog(logMessage);
            this.clusterIDs.remove(senderID);

            leaveRing(senderID);
            updateSuccessor();

            this.clusterIPs.remove(senderID);
            this.clusterPorts.remove(senderID);
        }
    }

    private void sendJoinMessage() throws UnknownHostException {
        final byte[] msg = JoinMessage.composeMessage(this.nodeID, membershipCounterManager.getMembershipCounter(),
                InetAddress.getLocalHost().getHostAddress(), this.tcpPort);
        this.multicastDispatcher.sendMessage(msg);
        System.out.println("Sent a JOIN message with contents " + new String(msg));
    }

    private void sendLeaveMessage() {
        final byte[] msg = LeaveMessage.composeMessage(this.nodeID, membershipCounterManager.getMembershipCounter());
        this.multicastDispatcher.sendMessage(msg);
        System.out.println("Sent a LEAVE message with contents " + new String(msg));
    }

    private void sendMembershipMessage(final String destinationIP, final int destinationPort) {
        final List<String> recent32LogEvents = logManager.get32MostRecentLogMessages();
        String logEvents = "";
        String clusterMembers = "";

        for (final String event : recent32LogEvents) {
            logEvents += event + "\n";
        }

        for (final String id : clusterIDs) {
            clusterMembers += id + "-";
        }

        logEvents = logEvents.substring(0, logEvents.length() - 1);
        clusterMembers = clusterMembers.substring(0, clusterMembers.length() - 1);

        final byte[] msg = MembershipMessage.composeMessage(this.nodeID, clusterMembers, logEvents);
        System.out.println("Sent a MEMBERSHIP message with contents " + new String(msg));
        this.tcpDispatcher.sendMessage(msg, destinationIP, destinationPort);
    }

    public void sendMulticastMembership() {
        final List<String> recent32LogEvents = logManager.get32MostRecentLogMessages();
        String logEvents = "";
        String clusterMembers = "";

        for (final String event : recent32LogEvents) {
            logEvents += event + "\n";
        }

        for (final String id : clusterIDs) {
            clusterMembers += id + "-";
        }

        logEvents = logEvents.length() == 0 ? "" : logEvents.substring(0, logEvents.length() - 1);
        clusterMembers = clusterMembers.substring(0, clusterMembers.length() - 1);

        final byte[] msg = MembershipMessage.composeMessage(this.nodeID, clusterMembers, logEvents);
        this.multicastDispatcher.sendMessage(msg);
        System.out.println("Sent a MEMBERSHIP MULTICAST message with contents " + new String(msg));
    }

    public void transferToSuccessor() {
        List<String> nodeFiles = storageManager.getFiles();
        String successorID = getSuccessor();
        String successorIP = this.clusterIPs.get(successorID);
        int successorPort = this.clusterPorts.get(successorID);
        for(String file : nodeFiles){
            String fileContents = storageManager.readFile(file);
            put(fileContents, successorIP, successorPort);
        }
    }

    public void put(String value, String ip, int port) {
        String hashValue = hashItem(value);

        if ((hashValue.compareTo(this.nodeHashValue) == 0) || (hashValue.compareTo(this.nodeHashValue) >= 0 && hashValue.compareTo(this.successorHash) < 0)
                || this.clusterIDs.size() == 1) {
            this.storageManager.writeFile(hashValue, value);

            // Send key as return message
            final byte[] msg = hashValue.getBytes();
            this.tcpDispatcher.sendMessage(msg, ip, port);
        } else {
            String correctNodeID = findCorrectNode(hashValue);
            final byte[] msg = PutMessage.composeMessage(value, ip, port);
            this.tcpDispatcher.sendMessage(msg, this.clusterIPs.get(correctNodeID),
                    this.clusterPorts.get(correctNodeID));
        }
    }

    public void get(String key, String ip, int port) {
        if ((key.compareTo(this.nodeHashValue) == 0) ||  (key.compareTo(this.nodeHashValue) >= 0 && key.compareTo(this.successorHash) < 0) || this.clusterIDs.size() == 1)  {
            // Send key as return message
            final byte[] msg = this.storageManager.readFile(key).getBytes();
            this.tcpDispatcher.sendMessage(msg, ip, port);
        } else {
            String correctNodeID = findCorrectNode(key);
            final byte[] msg = GetMessage.composeMessage(key, ip, port);
            this.tcpDispatcher.sendMessage(msg, this.clusterIPs.get(correctNodeID),
                    this.clusterPorts.get(correctNodeID));
        }
    }

    public void delete(String key, String ip, int port) {

        if ((key.compareTo(this.nodeHashValue) == 0) ||  (key.compareTo(this.nodeHashValue) >= 0 && key.compareTo(this.successorHash) < 0) || this.clusterIDs.size() == 1) {
            this.storageManager.deleteFile(key);
            // Send key as return message
            final byte[] msg = key.getBytes();
            this.tcpDispatcher.sendMessage(msg, ip, port);
        } else {
            String correctNodeID = findCorrectNode(key);
            final byte[] msg = DeleteMessage.composeMessage(key, ip, port);
            this.tcpDispatcher.sendMessage(msg, this.clusterIPs.get(correctNodeID),
                    this.clusterPorts.get(correctNodeID));
        }
    }

    private String findCorrectNode(String hashValue) {
        if (hashValue.equals(this.nodeHashValue)) {
            return this.nodeID;
        }
        for (int i = 0; i < this.clusterHashes.size(); i++) {
            if (hashValue.compareTo(this.clusterHashes.get(i)) == 0 || ( hashValue.compareTo(this.clusterHashes.get(i)) >= 0
                    && hashValue.compareTo(this.clusterHashes.get((i + 1) % this.clusterHashes.size())) < 0)) {
                for (String id : clusterIDs) {
                    String hash = hashItem(id);
                    if (hash.equals(this.clusterHashes.get(i))) {
                        return id;
                    }
                }
            }
        }
        return null;
    }

    private String getSuccessor() {
       String hashValue =  this.clusterHashes.get((this.clusterHashes.indexOf(this.nodeHashValue) + 1) % this.clusterHashes.size());
       for (String id : this.clusterIDs) {
           if (hashItem(id).equals(hashValue)) {
               return id;
           }
       }

       return null;
    }
}

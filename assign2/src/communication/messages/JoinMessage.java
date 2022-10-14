package communication.messages;

import store.Store;

public class JoinMessage extends Message {

    private final String[] header;
    private final Store node;

    public JoinMessage(Store node, String[] header) {
        this.header = header;
        this.node = node;

    }

    @Override
    public void handleMessage() {
        String senderID = this.header[1].split(":")[1];
        int membershipCounter = Integer.parseInt(this.header[2].split(":")[1]);
        int port = Integer.parseInt(this.header[3].split(":")[1]);
        String ip = this.header[4].trim().split(":")[1];
        node.receiveJoinMessage(senderID, membershipCounter, ip, port);

    }

    public static byte[] composeMessage(String nodeID, int membershipCounter, String ip, int port) {
        return ("JOIN id:" + nodeID + " membership:" + membershipCounter + " port:" + port + " ip:" + ip).getBytes();
    }
}

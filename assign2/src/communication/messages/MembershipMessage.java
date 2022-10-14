package communication.messages;

import store.Store;

public class MembershipMessage extends Message {

    private final Store node;
    private final String[] header;
    private final String body;

    public MembershipMessage(Store node, String[] header, String body) {
        this.node = node;
        this.header = header;
        this.body = body;
    }

    @Override
    public void handleMessage() {
        String senderID = this.header[1].split(":")[1];
        String members = this.header[2].split(":")[1];
        this.node.receiveMembershipMessage(senderID, members, body);
    }

    public static byte[] composeMessage(String nodeID, String members, String logData) {
        return ("MEMBERSHIP id:" + nodeID + " members:" + members + Message.CRLF + Message.CRLF + logData).getBytes();
    }
}

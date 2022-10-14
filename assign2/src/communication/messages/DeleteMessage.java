package communication.messages;

import store.Store;

import java.nio.charset.StandardCharsets;

public class DeleteMessage extends Message {

    private final String body;
    private final Store node;
    private final String[] header;

    public DeleteMessage(Store node, String[] messageHeader, String messageBody) {
        this.body = messageBody;
        this.node = node;
        this.header = messageHeader;
    }

    @Override
    public void handleMessage() {
        String ip = this.header[1].split(":")[1];
        int port = Integer.parseInt(this.header[2].trim().split(":")[1]);
        node.delete(this.body, ip, port);
    }

    public static byte[] composeMessage(String data, String ip, int port) {
        String string = "DELETE" + " ip:" + ip + " port:" + Integer.toString(port) + Message.CRLF + Message.CRLF + data;
        return string.getBytes(StandardCharsets.UTF_8);
    }
}

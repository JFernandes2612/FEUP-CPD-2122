package communication.messages;

import java.nio.charset.StandardCharsets;

import store.Store;

public class GetMessage extends Message {

    private final String body;
    private final Store node;
    private final String[] header;

    public GetMessage(Store node, String[] messageHeader, String messageBody) {
        this.node = node;
        this.body = messageBody;
        this.header = messageHeader;
    }

    @Override
    public void handleMessage() {
        String ip = this.header[1].split(":")[1];
        int port = Integer.parseInt(this.header[2].trim().split(":")[1]);
        node.get(this.body, ip, port);
    }

    public static byte[] composeMessage(String data, String ip, int port) {
        String string = "GET" + " ip:" + ip + " port:" + Integer.toString(port) + Message.CRLF + Message.CRLF + data;
        return string.getBytes(StandardCharsets.UTF_8);
    }
}

package communication.messages;

import store.Store;

import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;

public class PutMessage extends Message {
    private final String body;
    private final Store node;
    private final String[] header;

    public PutMessage(Store node, String[] header, String body) {
        this.body = body;
        this.node = node;
        this.header = header;
    }

    @Override
    public void handleMessage() throws RemoteException {
        String ip = this.header[1].split(":")[1];
        int port = Integer.parseInt(this.header[2].trim().split(":")[1]);
        node.put(this.body, ip, port);
    }

    public static byte[] composeMessage(String fileName, String ip, int port) {
        String string = "PUT" + " ip:" + ip + " port:" + Integer.toString(port) + Message.CRLF + Message.CRLF
                + fileName;
        return string.getBytes(StandardCharsets.UTF_8);
    }

}

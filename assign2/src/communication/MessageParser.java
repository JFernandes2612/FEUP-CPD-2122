package communication;

import java.rmi.RemoteException;

import communication.messages.Message;
import store.Store;

/**
 * Class that will be run inside the TCP and {@link MulticastDispatcher} Will
 * parse the message and handle with the appropriate one
 */
public class MessageParser implements Runnable {

    private final byte[] msg;
    private final Store node;

    public MessageParser(byte[] msg, Store node) {
        this.msg = msg;
        this.node = node;
    }

    @Override
    public void run() {
        Message message = Message.parseMessage(msg, node);
        try {
            message.handleMessage();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

package communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import store.Store;

public class MulticastDispatcher extends Thread {

    private final ExecutorService executorService;
    private final Store node;
    private final MulticastSocket socket;
    private final byte[] buf;
    private final SocketAddress address;
    private final int port;
    private final String ip;
    private boolean working;

    public MulticastDispatcher(final String ip, final int port, final Store node) throws IOException {
        this.executorService = Executors.newCachedThreadPool();
        this.node = node;
        this.buf = new byte[512];
        this.socket = new MulticastSocket(port);
        this.socket.setSoTimeout(1000);
        this.address = new InetSocketAddress(ip, port);
        this.ip = ip;
        this.port = port;
        final NetworkInterface networkInterface = NetworkInterface.getByName(ip);
        this.socket.joinGroup(address, networkInterface);
        this.working = true;
    }

    public void stopLoop() {
        this.working = false;
    }

    /**
     * Keep loop tight to be highly available. Use threads to handle everything
     */
    @Override
    public void run() {
        while (this.working) {
            final DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
                executorService.submit(new MessageParser(packet.getData(), node));
            } catch (final IOException e) {
            }
        }
        System.out.println("Multicast socket shutting down");
    }

    public void sendMessage(final byte[] msg) {
        InetAddress address;
        try {
            address = InetAddress.getByName(this.ip);
            final DatagramPacket packet = new DatagramPacket(msg, msg.length, address, port);

            socket.send(packet);
        } catch (final IOException e) {
            System.out.println("Error sending Multicast Messages: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

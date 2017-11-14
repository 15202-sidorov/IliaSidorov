import jdk.nashorn.internal.ir.Block;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class MYServerSocket {
    public MYServerSocket(int port, BlockingQueue<DatagramPacket> inputQueue) throws SocketException {
        UDPSocket = new DatagramSocket(port);
        connectionsStatuses = Collections.synchronizedMap(new HashMap<InetSocketAddress, ConnectionStatus>());
        newConnectionPackets = inputQueue;
    }


    public MYSocket accept() throws IOException, InterruptedException {
        DatagramPacket packetReceived = newConnectionPackets.take();
        //configure MYSocket
    }


    //closing connection with everybody
    public void close() {

    }

    private DatagramSocket UDPSocket;
    private Map<InetSocketAddress, ConnectionStatus> connectionsStatuses;
    private BlockingQueue<DatagramPacket> newConnectionPackets;

    private static final short CONNECTIONS_ALLOWED = 5;
}

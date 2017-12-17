/*

    Server side socket.

    Accept method establishes incoming connection and then returns MYSocket, through which data is transmitted.
    Close method closes all previously established connections.


*/

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MYServerSocket {
    public MYServerSocket( int port ) throws Exception {
        UDPSocket = new DatagramSocket(port,InetAddress.getLocalHost());
        connectionsStatuses = Collections.synchronizedMap( new HashMap<InetSocketAddress, ConnectionStatus>() );
        newConnectionPackets = new ArrayBlockingQueue<DatagramPacket>(PACKET_QUEUE_DEFAULT_CAPACITY);
        receivingThread = new ReceivingThread(UDPSocket, newConnectionPackets, connectionsStatuses);
        receivingThread.start();
        pingTimer = new Timer();

        System.out.println("Server socket is initialized, socket address is : " + UDPSocket.getLocalSocketAddress());
    }

    private class pingTimerTask extends TimerTask {
        public void run() {
            for (InetSocketAddress address : connectionsStatuses.keySet()) {
                if ( connectionsStatuses.get(address).getStatus() == Status.CLOSED ) {
                    connectionsStatuses.remove(address);
                }
            }
        }
    }

    public MYClientSocketOnServerSide accept() throws IOException, InterruptedException {
        DatagramPacket packetReceived = newConnectionPackets.take();
        InetSocketAddress receivedFrom = (InetSocketAddress) packetReceived.getSocketAddress();
        /*
            Making connection.
        */

        byte[] packetData = packetReceived.getData();
        short flag = PacketConstructor.getFlag(packetData);
        ConnectionStatus currentStatus = new ConnectionStatus(receivedFrom);
        /*
            Client tries to init connection.
        */

        currentStatus.setStatus(Status.LISTEN);
        byte[] synAckPacketData = PacketConstructor.buildSYN( true);
        DatagramPacket synAckPacket = new DatagramPacket(synAckPacketData, synAckPacketData.length);
        synAckPacket.setSocketAddress(receivedFrom);
        UDPSocket.send(synAckPacket);

        /*
            Configure MYSocket to transmit data.
        */

        connectionsStatuses.put(receivedFrom, currentStatus);

        System.out.println("Listening to new socket...");
        return new MYClientSocketOnServerSide(UDPSocket, currentStatus);
    }

    /*
        Closing all connections.
    */

    public void close() {
        System.out.println("Closing server socket");
    }



    private ReceivingThread receivingThread;

    private DatagramSocket UDPSocket;
    private Map<InetSocketAddress, ConnectionStatus> connectionsStatuses;
    private BlockingQueue<DatagramPacket> newConnectionPackets;
    private BlockingQueue<DatagramPacket> oldConnectionPackets;
    private ByteBuffer socketBuffer;
    private Timer pingTimer;

    private static final short PACKET_QUEUE_DEFAULT_CAPACITY = 20;
}

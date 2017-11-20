/*

    Server side socket.

    Accept method establishes incoming connection and then returns MYSocket, through which data is transmitted.
    Close method closes all previously established connections.


*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class MYServerSocket {
    public MYServerSocket( int port ) throws SocketException {
        UDPSocket = new DatagramSocket(port);
        currentConnectionsCount = 0;
        connectionsStatuses = Collections.synchronizedMap( new HashMap<InetSocketAddress, ConnectionStatus>() );
        newConnectionPackets = new ArrayBlockingQueue<DatagramPacket>(PACKET_QUEUE_DEFAULT_CAPACITY);
        oldConnectionPackets = new ArrayBlockingQueue<DatagramPacket>(PACKET_QUEUE_DEFAULT_CAPACITY);
        receivingThread = new ReceivingThread(UDPSocket, newConnectionPackets, connectionsStatuses);
        receivingThread.start();
        socketBuffer = ByteBuffer.allocate(SERVER_SOCKET_BUFFER_SIZE);
    }

    public MYSocket accept() throws IOException, InterruptedException {
        DatagramPacket packetReceived = newConnectionPackets.take();
        InetSocketAddress receivedFrom = (InetSocketAddress) packetReceived.getSocketAddress();
        /*
            Making connection.
        */

        makeConnection( packetReceived );

        /*
            Configure MYSocket to transmit data.
        */

        ConnectionStatus newConnectionStatus = new ConnectionStatus(receivedFrom);
        connectionsStatuses.put(receivedFrom, newConnectionStatus);
        establishedConnections.add(receivedFrom);
        currentConnectionsCount++;
        return new MYClientSocketOnServerSide((InetSocketAddress) packetReceived.getSocketAddress(), UDPSocket, newConnectionStatus);
    }

    /*
        Closing all connections.
    */

    public void close() {

    }

    /*
        Establishes connection with the client.
    */

    private void makeConnection( DatagramPacket packetReceived ) throws IOException, InterruptedException {
        InetSocketAddress receivedFrom = (InetSocketAddress) packetReceived.getSocketAddress();
        byte[] packetData = packetReceived.getData();
        short flag = PacketConstructor.getFlag(packetData);
        ConnectionStatus currentStatus = connectionsStatuses.get(receivedFrom);
        /*
            Client tries to init connection.
        */
        if ( flag == Flags.SYN_FLAG ) {
            currentStatus.setStatus(Status.SYN_RECEIVED);
            byte[] synAckPacketData = PacketConstructor.buildSYN( true);
            DatagramPacket synAckPacket = new DatagramPacket(synAckPacketData, synAckPacketData.length);
            synAckPacket.setSocketAddress(receivedFrom);
            UDPSocket.send(synAckPacket);
        }
        else {
            System.out.println("Not a protocol behavior...");
            return;
        }

        LinkedList<DatagramPacket> packetsForOthers = new LinkedList<DatagramPacket>();
        while ( true ) {
            packetReceived = newConnectionPackets.take();
            /*
                In case some new connection is initialized.
            */
            if ( !receivedFrom.equals(packetReceived.getSocketAddress()) ) {
                packetsForOthers.add(packetReceived);
            }
            else {
                newConnectionPackets.addAll(packetsForOthers);
                break;
            }
        }



        /*
            Connection is successfully established.
        */
        if ( flag == (Flags.SYN_FLAG | Flags.ACK_FLAG) ) {
            currentStatus.setStatus(Status.ESTABLISHED);
        }
        else if ( flag == (Flags.SYN_FLAG | Flags.RST_FLAG) ) {
            System.out.println("Client terminated request");
            return;
        }
        else {
            System.out.println("Not a protocol behavior");
            currentStatus.setStatus(Status.CLOSED);
        }

    }

    private ReceivingThread receivingThread;

    private DatagramSocket UDPSocket;
    private Map<InetSocketAddress, ConnectionStatus> connectionsStatuses;
    private BlockingQueue<DatagramPacket> newConnectionPackets;
    private BlockingQueue<DatagramPacket> oldConnectionPackets;
    private List<InetSocketAddress> establishedConnections;
    private ByteBuffer socketBuffer;
    private short currentConnectionsCount;

    private static final short CONNECTIONS_ALLOWED = 5;
    private static final short PACKET_QUEUE_DEFAULT_CAPACITY = 20;
    private static final short SERVER_SOCKET_BUFFER_SIZE = 64;
}

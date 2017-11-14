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
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class MYServerSocket {
    public MYServerSocket( int port ) throws SocketException {
        UDPSocket = new DatagramSocket(port);

        connectionsStatuses = Collections.synchronizedMap( new HashMap<InetSocketAddress, ConnectionStatus>() );
        establishedConnections = Collections.synchronizedList(new LinkedList<InetSocketAddress>());
        newConnectionPackets = new SynchronousQueue<DatagramPacket>();
        oldConnectionPackets = new SynchronousQueue<DatagramPacket>();
        receivingThread = new ReceivingThread(UDPSocket, newConnectionPackets, oldConnectionPackets, establishedConnections);
        receivingThread.start();
    }

    public MYSocket accept() throws IOException, InterruptedException {
        DatagramPacket packetReceived = newConnectionPackets.take();
        InetSocketAddress receivedFrom = (InetSocketAddress) packetReceived.getSocketAddress();
        /*

            Making connection.

        */



        /*

            Checking, if connection is still in cache.
            Configure MYSocket to transmit data.

        */

        if ( connectionsStatuses.containsKey(receivedFrom) ) {
            ConnectionStatus connectionFromCache = connectionsStatuses.get(receivedFrom);
            connectionFromCache.setStatus(Status.CLOSED);
            establishedConnections.add(receivedFrom);
            return new MYSocket((InetSocketAddress) packetReceived.getSocketAddress(), UDPSocket, connectionFromCache);
        }
        else {
            ConnectionStatus newConnectionStatus = new ConnectionStatus(receivedFrom);
            connectionsStatuses.put(receivedFrom, newConnectionStatus);
            establishedConnections.add(receivedFrom);
            return new MYSocket((InetSocketAddress) packetReceived.getSocketAddress(), UDPSocket, newConnectionStatus);

        }
    }

    /*

        Closing connections.
        
    */
    public void close() {

    }

    private ReceivingThread receivingThread;

    private DatagramSocket UDPSocket;
    private Map<InetSocketAddress, ConnectionStatus> connectionsStatuses;
    private BlockingQueue<DatagramPacket> newConnectionPackets;
    private BlockingQueue<DatagramPacket> oldConnectionPackets;
    private List<InetSocketAddress> establishedConnections;
    private static final short CONNECTIONS_ALLOWED = 5;
}

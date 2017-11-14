/*

    Receiving thread receives all incoming packets from network.
    If the connection is currently in non closed state, received packet is put in oldConnectionPackets,
    Packet is put in newConnectionsPackets otherwise.

 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;


public class ReceivingThread extends Thread {
    public ReceivingThread(DatagramSocket inputUDPSocket,
                           BlockingQueue<DatagramPacket> inputNewConnections,
                           BlockingQueue<DatagramPacket> inputOldConnections,
                           List<InetSocketAddress> inputEstablishedConnections) {
        newConnectionsPackets = inputNewConnections;
        oldConnectionsPackets = inputOldConnections;
        UDPSocket = inputUDPSocket;
        establishedConncetions =inputEstablishedConnections;
    }

    public void run() {
        try {
            byte[] data = new byte[PacketConstructor.getPacketSize()];
            DatagramPacket packetReceived = new DatagramPacket(data, data.length);
            UDPSocket.receive(packetReceived);
            InetSocketAddress receivedFrom = (InetSocketAddress) packetReceived.getSocketAddress();
            if ( establishedConncetions.contains(receivedFrom) ) {
                oldConnectionsPackets.put(packetReceived);
            }
            else {
                newConnectionsPackets.put(packetReceived);
            }
        }
        catch ( IOException ex ) {
            ex.printStackTrace();
        }
        catch ( InterruptedException ex ) {
            Thread.currentThread().interrupt();
        }
    }

    private DatagramSocket UDPSocket;
    private BlockingQueue<DatagramPacket> newConnectionsPackets;
    private BlockingQueue<DatagramPacket> oldConnectionsPackets;
    private List<InetSocketAddress> establishedConncetions;
}

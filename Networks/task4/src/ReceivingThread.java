/*

    Receiving thread receives all incoming packets from network.
    If the connection is currently in non closed state, received packet is put in oldConnectionPackets,
    Packet is put in newConnectionsPackets otherwise.

 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.BlockingQueue;


public class ReceivingThread extends Thread {
    public ReceivingThread(DatagramSocket inputUDPSocket,
                           BlockingQueue<DatagramPacket> inputNewConnections,
                           Map<InetSocketAddress, ConnectionStatus> inputAllConnections) {
        newConnectionsPackets = inputNewConnections;
        allConnections = inputAllConnections;
        UDPSocket = inputUDPSocket;
    }

    public void run() {
        try {
            while ( !Thread.currentThread().isInterrupted() ) {
                byte[] data = new byte[PacketConstructor.getHeaderSize() + BUFFER_SIZE];

                DatagramPacket packetReceived = new DatagramPacket(data, data.length);
                UDPSocket.receive(packetReceived);
                InetSocketAddress receivedFrom = (InetSocketAddress) packetReceived.getSocketAddress();
                if ( allConnections.containsKey(receivedFrom) ) {
                    allConnections.get(receivedFrom).packetReceived(packetReceived);
                }
                else if ( (PacketConstructor.getFlag(packetReceived.getData()) == Flags.SYN_FLAG) ) {
                    newConnectionsPackets.put(packetReceived);
                }
                else {
                    System.out.println("Suspicious packet received, dropping ...");
                }
            }
        }
        catch ( IOException ex ) {
            ex.printStackTrace();
        }
        catch ( InterruptedException ex ) {
            System.out.println("Interrupted");
            Thread.currentThread().interrupt();
        }
    }

    private DatagramSocket UDPSocket;
    private BlockingQueue<DatagramPacket> newConnectionsPackets;
    private BlockingQueue<DatagramPacket> oldConnectionsPackets;
    private Map<InetSocketAddress, ConnectionStatus> allConnections;

    private final short BUFFER_SIZE = 1024;
}

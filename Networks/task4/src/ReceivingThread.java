import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;


public class ReceivingThread extends Thread {
   public ReceivingThread( DatagramSocket inputUDPSocket,
                           BlockingQueue<DatagramPacket> inputNewConnections,
                           BlockingQueue<DatagramPacket> inputOldConnections ) {
       newConnectionsPackets = inputNewConnections;
       oldConnectionsPackets = inputOldConnections;
       UDPSocket = inputUDPSocket;
   }

   public void run() {
       try {
           byte[] data = new byte[PacketConstructor.getPacketSize()];
           DatagramPacket packetReceived = new DatagramPacket(data, data.length);
           UDPSocket.receive(packetReceived);
           short flag = PacketConstructor.getFlag(packetReceived.getData());
           if ( flag == (Flags.SYQ_FLAG | Flags.ACK_FLAG) ) {
                newConnectionsPackets.put(packetReceived);
           }
           else {
               oldConnectionsPackets.put(packetReceived);
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
}

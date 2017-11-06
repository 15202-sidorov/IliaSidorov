/*

    Connection handler sends and receives all types of
        messages in protocol.
    It deals with sockets and already combined by PacketHandler UDP packets.

 */

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.Random;

public class ConnectionHandler {
    public ConnectionHandler(User inputNodeUser,
                             DatagramSocket inputNodeSocket,
                             Map<InetSocketAddress, SiblingStatus> inputSiblingStatus) {
        nodeUser = inputNodeUser;
        nodeSocket = inputNodeSocket;
        siblingStatus = inputSiblingStatus;
        randomGenerator = new Random();
    }


    public void sendPACKET( DatagramPacket packet ) throws InterruptedException, IOException {
        InetSocketAddress destination = (InetSocketAddress) packet.getSocketAddress();
        if ( !siblingStatus.containsKey(destination) ) {
            System.out.println("Tried to send packet to unregistered node");
            return;
        }

        if ( PacketType.ACK != PacketHandler.getPacketType(packet.getData()) ) {
            siblingStatus.get(destination).pushToPacketQueue(packet);
        }

        if ( !siblingStatus.get(destination).isAvailable() ) {
           // System.out.println("Node " + destination + " is not available at the moment");
            return;
        }
      //  System.out.println("Node " + destination + " is available, sending packet");
        nodeSocket.send(packet);
        if (PacketHandler.getPacketType(packet.getData()) != PacketType.ACK) {
            siblingStatus.get(destination).noAck();
        }
    }

    public void sendPACKET(InetSocketAddress destination, short type) throws IOException, InterruptedException {
        if (PacketType.exists(type)) {
            if ((type == PacketType.TEXT) || (type == PacketType.PARENT)) {
                System.out.println("Trying to send TEXT or PARENT  packet via incorrect method");
                return;
            }
            else if ( !siblingStatus.containsKey(destination) ) {
                System.out.println("Trying to send packet to an unregistered destination ");
                return;
            }
            else {
                byte[] data = PacketHandler.constructPacket(nodeUser.getID(), type);
                DatagramPacket packet = new DatagramPacket(data, data.length);
                packet.setSocketAddress(destination);
                if ( type != PacketType.ACK ) {
                    siblingStatus.get(destination).pushToPacketQueue(packet);
                }
                if ( !siblingStatus.get(destination).isAvailable() ) {
                   // System.out.println("Node " + destination + " is not available at the moment");
                    return;
                }
                // System.out.println("Node " + destination + " is available, sending packet");
                nodeSocket.send(packet);
                if (type != PacketType.ACK) {
                    siblingStatus.get(destination).noAck();
                }
            }
        }
        else {
            System.out.println("Packet of such type does not exist , dropping");
        }
    }

    public DatagramPacket receivePacket() throws IOException, InterruptedException {
        byte[] data = new byte[INITIAL_BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(data, data.length);
       // System.out.println("Receiving packet...");
        nodeSocket.receive(packet);
        boolean packetIsLost = (randomGenerator.nextInt(100) > LOST_PERSENT) && (PacketHandler.getPacketType(packet.getData()) != PacketType.ACK);
        if ( packetIsLost ) {
            return null;
        }

        boolean packetIsAck = PacketHandler.getPacketType(packet.getData()) == PacketType.ACK;
        InetSocketAddress recievedFrom = (InetSocketAddress)packet.getSocketAddress();
        if ( siblingStatus.containsKey( packet.getSocketAddress()) ) {
            siblingStatus.get( packet.getSocketAddress() ).gotPing();
        }

        if ( !packetIsAck ) {
            sendPACKET((InetSocketAddress)packet.getSocketAddress(), PacketType.ACK);
        }

        return packet;
    }

    public void sendPARENT(InetSocketAddress destination,
                           InetSocketAddress parent) throws IOException, InterruptedException {
        if ( !siblingStatus.containsKey(destination) ) {
            System.out.println("Trying to send packet to an unregistered destination");
            return;
        }

        byte[] data = PacketHandler.constructParentPacket(nodeUser.getID(), parent);
        DatagramPacket packet = new DatagramPacket(data, data.length);
        packet.setSocketAddress(destination);
        siblingStatus.get(destination).pushToPacketQueue(packet);
        if ( !siblingStatus.get(destination).isAvailable() ) {
           // System.out.println("Node " + destination + " is not available at the moment");
            return;
        }

        // System.out.println("Node " + destination + " is available, sending packet");
        nodeSocket.send(packet);
        siblingStatus.get(destination).noAck();
    }

    public void sendCONNECT(InetSocketAddress destination) throws IOException, InterruptedException {
        if ( !siblingStatus.containsKey(destination) ) {
            siblingStatus.put(destination, new SiblingStatus(destination));
            sendPACKET(destination, PacketType.CONNECT);
        }
        else {
            sendPACKET(destination, PacketType.CONNECT);
        }
    }

    public void sendTEXT(InetSocketAddress destination, String text) throws IOException, InterruptedException {
        if ( !siblingStatus.containsKey(destination) ) {
            System.out.println("Trying to send packet to an unregistered destination");
            return;
        }

        byte[] data = PacketHandler.constructTextPacket(nodeUser, text);
        DatagramPacket packet = new DatagramPacket(data, data.length);
        packet.setSocketAddress(destination);
        siblingStatus.get(destination).pushToPacketQueue(packet);

        if ( !siblingStatus.get(destination).isAvailable() ) {
         //   System.out.println("Node " + destination + " is not available at the moment");
            return;
        }

       // System.out.println("Node " + destination + " is available, sending packet");
        nodeSocket.send(packet);
        siblingStatus.get(destination).noAck();
    }

    private User nodeUser;
    private DatagramSocket nodeSocket;
    private Map<InetSocketAddress, SiblingStatus> siblingStatus;
    private Random randomGenerator;

    private static final int INITIAL_BUFFER_SIZE = 64;
    private static final int LOST_PERSENT = 90;
}

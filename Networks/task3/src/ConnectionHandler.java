/*

    Connection handler sends and receives all types of
    messages in protocol.

    Deals with packages.

 */


//!! no packet queue many queues for each connection
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.SynchronousQueue;

public class ConnectionHandler {
    public ConnectionHandler(User inputNodeUser,
                             DatagramSocket inputNodeSocket,
                             SynchronousQueue<DatagramPacket> inputPacketQueue,
                             HashMap<InetSocketAddress, SiblingStatus> inputSiblingStatus) {
        nodeUser = inputNodeUser;
        nodeSocket = inputNodeSocket;
        packetQueue = inputPacketQueue;
        siblingStatus = inputSiblingStatus;
    }

    public void sendPACKET(InetSocketAddress destination, short type) throws IOException, InterruptedException {
        if (PacketType.exists(type)) {
            if ((type == PacketType.TEXT) || (type == PacketType.PARENT)) {
                return;
            }
            else if ( !siblingStatus.containsKey(destination) ) {
                return;
            }
            else {
                byte[] data = PacketHandler.constructPacket(nodeUser.getID(), PacketType.CONNECT);
                DatagramPacket packet = new DatagramPacket(data, data.length);
                packet.setSocketAddress(destination);
                if ( !siblingStatus.get(destination).isAvailable() ) {
                    packetQueue.put(packet);
                    return;
                }
                else {
                    nodeSocket.send(packet);
                    if (type != PacketType.ACK) {
                        siblingStatus.get(destination).noAck();
                    }
                    return;
                }
            }
        }
    }

    public DatagramPacket receivePacket() throws IOException {
        byte[] data = new byte[INITIAL_BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        nodeSocket.receive(packet);

        return packet;
    }

    public void sendPARENT(InetSocketAddress destination,
                           InetSocketAddress parent) throws IOException, InterruptedException {
        if ( !siblingStatus.containsKey(destination) ) {
            return;
        }

        byte[] data = PacketHandler.constructParentPacket(nodeUser.getID(), parent);
        DatagramPacket packet = new DatagramPacket(data, data.length);
        packet.setSocketAddress(destination);
        if ( !siblingStatus.get(destination).isAvailable() ) {
            packetQueue.put(packet);
            return;
        }

        nodeSocket.send(packet);
        siblingStatus.get(destination).noAck();
    }

    public void sendTEXT(InetSocketAddress destination,
                            String text) throws IOException, InterruptedException {
        if ( !siblingStatus.containsKey(destination) ) {
            return;
        }

        byte[] data = PacketHandler.constructTextPacket(nodeUser, text);
        DatagramPacket packet = new DatagramPacket(data, data.length);
        packet.setSocketAddress(destination);

        if ( !siblingStatus.get(destination).isAvailable() ) {
            packetQueue.put(packet);
            return;
        }

        nodeSocket.send(packet);
        siblingStatus.get(destination).noAck();
    }

    private User nodeUser;
    private DatagramSocket nodeSocket;
    private SynchronousQueue<DatagramPacket> packetQueue;
    private HashMap<InetSocketAddress, SiblingStatus> siblingStatus;

    private static final int INITIAL_BUFFER_SIZE = 64;
}

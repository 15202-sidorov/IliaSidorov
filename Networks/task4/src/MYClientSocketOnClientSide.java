import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MYClientSocketOnClientSide extends MYClientSocket{
    public MYClientSocketOnClientSide(DatagramSocket socket,
                                      ConnectionStatus status) {
        super(socket,status);
        connectionsStatuses = Collections.synchronizedMap( new HashMap<InetSocketAddress, ConnectionStatus>() );
        connectionsStatuses.put(status.getAddress(), status);
        newConnectionPackets = new ArrayBlockingQueue<DatagramPacket>(PACKET_QUEUE_DEFAULT_CAPACITY);
        receivingThread = new ReceivingThread(UDPSocket, newConnectionPackets, connectionsStatuses);
        receivingThread.start();
    }

    public void connect() throws IOException, MYSocketException, InterruptedException {
        byte[] packetData = PacketConstructor.buildSYN(false);
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length);
        packet.setSocketAddress(currentStatus.getAddress());
        UDPSocket.send(packet);
        packet = currentStatus.pollPacket();
        if ( PacketConstructor.getFlag(packet.getData()) == (Flags.ACK_FLAG | Flags.SYN_FLAG) ) {
            System.out.println("Connection is established");
            currentStatus.setStatus(Status.LISTEN);
        }
        else {
            System.out.println("Connected to server");
            throw new MYSocketException();
        }
    }

    private Thread receivingThread;
    private BlockingQueue<DatagramPacket> newConnectionPackets;
    private Map<InetSocketAddress, ConnectionStatus> connectionsStatuses;

    private static final short PACKET_QUEUE_DEFAULT_CAPACITY = 20;

}

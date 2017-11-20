
import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

//Concurrent ByteBuffer!!!!
//Should be in a

public class MYClientSocketOnServerSide implements MYSocket {
    public MYClientSocketOnServerSide(InetSocketAddress inputAddress,
                    DatagramSocket serverUDPSocket,
                    ConnectionStatus inputStatus) {
        remoteAddress = inputAddress;
        currentStatus = inputStatus;
        sequenceNumber = 0;
        ackNumber = 0;
    }

    public void send(byte[] dataToSend) throws IOException, InterruptedException {
        byte[] data = PacketConstructor.buildDEFAULT(sequenceNumber, dataToSend);
        DatagramPacket packetToSend = new DatagramPacket(data, data.length);
        packetToSend.setSocketAddress(remoteAddress);
        UDPSocket.send(packetToSend);
        sequenceNumber++;
        DatagramPacket packetReceived = null;
        while ( true ) {
            packetReceived = currentStatus.pollPacket();
            if ( PacketConstructor.getSeq(packetReceived.getData()) != packetsReceivedCount ) {
                currentStatus.packetReceived(packetReceived);
            }
            else {
                break;
            }
        }

        if ( PacketConstructor.getFlag(packetReceived.getData()) != Flags.ACK_FLAG ) {
            System.out.println("Not a protocol behavior, dropping...");
        }
        else if (PacketConstructor.getAck(packetReceived.getData()) != sequenceNumber - 1) {
            System.out.println("Wrong sequence number received"); //???????
        }

    }

    public byte[] receive() throws IOException {

        byte[] result = PacketConstructor.buildDEFAULT(0, new byte[BUFFER_SIZE]);
        return result;
    }

    public void close() {

    }


    private InetSocketAddress remoteAddress;
    private ConnectionStatus currentStatus;
    private DatagramSocket UDPSocket;
    private int sequenceNumber = 0;
    private int packetsReceivedCount = 0;
    private int ackNumber = 0;

    private static short BUFFER_SIZE = 64;

}



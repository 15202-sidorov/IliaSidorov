import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.LinkedList;

/*

    Each connection has its own status.
    Status consists of :
        -- Buffer ( Buffer of messages with wrong sequence numbers )
        -- Internet address
        -- Connection status ( see wikipedia )
        -- Bytes ( received / send )

 */

public class ConnectionStatus {
    public ConnectionStatus( InetSocketAddress statusFor ) {
        address = statusFor;
        bytesReceived = 0;
        sessionStatus = Status.CLOSED;
    }

    public void setStatus( Status newStatus ) {
        sessionStatus = newStatus;
    }

    public Status getStatus() {
        return sessionStatus;
    }

    /*

        Finds packet in buffer with minimal sequence number.

    */

    public DatagramPacket findPacketInBufferIfAvailable() {
        if ( packetsToHandle.size() == 0 ) {
            return null;
        }
        else {
            int min = PacketConstructor.getSeq(packetsToHandle.get(0).getData());
            DatagramPacket result = packetsToHandle.get(0);
            for (int i = 1; i < packetsToHandle.size(); i++) {
                int currentSeqNumber = PacketConstructor.getSeq(packetsToHandle.get(i).getData());
                if ( min > currentSeqNumber ) {
                    min = currentSeqNumber;
                    result = packetsToHandle.get(i);
                }
            }

            return result;
        }
    }

    private InetSocketAddress address;
    private LinkedList<DatagramPacket> packetsToHandle;
    private long bytesReceived;
    private Status sessionStatus;
}

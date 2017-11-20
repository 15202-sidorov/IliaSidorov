import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/*

    Each connection has its own status.
    Status consists of :
        -- Buffer ( Buffer of messages with wrong sequence numbers )
        -- Internet address
        -- Connection status ( see wikipedia )
        -- Bytes ( received / send ) !!!!!!!!!!!ConcurrentByteBuffer

 */

public class ConnectionStatus {
    public ConnectionStatus( InetSocketAddress statusFor ) {
        address = statusFor;
        sessionStatus = Status.CLOSED;
        packetsToHandle = new ArrayBlockingQueue<DatagramPacket>(20);
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

    public DatagramPacket pollPacket() {
        return packetsToHandle.poll();
    }

    public void packetReceived(DatagramPacket packet) {
        packetsToHandle.add(packet);
    }

    private InetSocketAddress address;
    private BlockingQueue<DatagramPacket> packetsToHandle;

    private Status sessionStatus;
}

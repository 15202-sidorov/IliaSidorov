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
        isAlive = true;
    }

    public void setStatus( Status newStatus ) {
        sessionStatus = newStatus;
    }

    public Status getStatus() {
        return sessionStatus;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    /*
        Finds packet in buffer with minimal sequence number.
    */

    public DatagramPacket pollPacket() throws InterruptedException {
        return packetsToHandle.take();
    }

    public void packetReceived(DatagramPacket packet) {
        isAlive = true;
        packetsToHandle.offer(packet);
    }

    public boolean isAlive;

    private InetSocketAddress address;
    private BlockingQueue<DatagramPacket> packetsToHandle;

    private Status sessionStatus;
}

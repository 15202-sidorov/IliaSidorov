import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/*
    Each connection has its own status.
    Status consists of :
        -- Buffer ( messages received out of order which should be pushed later )
        -- Internet address
        -- Connection status ( see wikipedia )
        -- Bytes ( received / send )

 */

public class ConnectionStatus {
    public ConnectionStatus( InetSocketAddress statusFor ) {
        address = statusFor;
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        bytesReceived = 0;
        sessionStatus = Status.CLOSED;
    }


    private InetSocketAddress address;
    private ByteBuffer buffer;
    private long bytesReceived;
    private Status sessionStatus;

    private static final short BUFFER_SIZE = 64;
}

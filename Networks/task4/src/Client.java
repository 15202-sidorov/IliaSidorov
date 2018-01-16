import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Client {
    public static void main( String[] args ) {
        try {
            UDPsocket = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(), 0));

            status = new ConnectionStatus(new InetSocketAddress(InetAddress.getLocalHost(), 8000));
            MYClientSocketOnClientSide MYSocket = new MYClientSocketOnClientSide(UDPsocket,status);
            MYSocket.connect();
            byte[] data = MYSocket.receive();
            System.out.println(Charset.defaultCharset().decode(ByteBuffer.wrap(data)));
            data = MYSocket.receive();
            System.out.println(Charset.defaultCharset().decode(ByteBuffer.wrap(data)));

        }
        catch( Exception ex ) {
            ex.printStackTrace();
        }
    }

    private static DatagramSocket UDPsocket;
    private static ConnectionStatus status;
}

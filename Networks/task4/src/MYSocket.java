import java.net.*;

public class MYSocket {
    //created on server side
    public MYSocket(InetSocketAddress inputAddress, DatagramSocket inputSocket) {
        address = inputAddress; // in this case, this is remote address of socket
        mainSocket = inputSocket;
    }


    //created on client side
    public MYSocket() throws SocketException {
        address = new InetSocketAddress(0); // local address of socket
        mainSocket = new DatagramSocket(address);
        address = (InetSocketAddress) mainSocket.getLocalSocketAddress();
    }

    public void connect( InetSocketAddress inputServerAddress ) {
        serverAddress = inputServerAddress;
    }

    public void send() {

    }

    public void receive() {

    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void close() {

    }

    private InetSocketAddress address;
    private InetSocketAddress serverAddress;
    private DatagramSocket mainSocket;
}

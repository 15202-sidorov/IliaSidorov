/*

    Should be used on clients side, in order to implement client's behavior.
    Can also be used on server side, to send packets to client.

    Connect method is used on client's side only.

 */

import java.net.*;

public class MYSocket {
    /*

        Created on server's side.

    */

    public MYSocket(InetSocketAddress inputAddress, DatagramSocket inputSocket, ConnectionStatus inputStatus) {
        remoteAddress = inputAddress; // in this case, this is remote address of socket
        mainSocket = inputSocket;
        currentStatus = inputStatus;
        serverAddress = (InetSocketAddress) inputSocket.getLocalSocketAddress();
    }

    /*

        Created on client's side

    */

    public MYSocket() throws SocketException {
        remoteAddress = new InetSocketAddress(0); // local address of socket
        mainSocket = new DatagramSocket(remoteAddress);
        remoteAddress = (InetSocketAddress) mainSocket.getLocalSocketAddress();
        currentStatus = new ConnectionStatus(remoteAddress);
    }

    public void connect( InetSocketAddress inputServerAddress ) {
        serverAddress = inputServerAddress;
    }

    public void send() {

    }

    public void receive() {

    }

    public InetSocketAddress getAddress() {
        return remoteAddress;
    }

    public void close() {

    }

    private InetSocketAddress remoteAddress;
    private InetSocketAddress serverAddress;
    private DatagramSocket mainSocket;
    private ConnectionStatus currentStatus;
}

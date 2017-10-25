
import java.net.*;
import java.util.UUID;

public class MainThread {
    public static void main( String args[] ) {
        String nickname = "IliaSidorov";
        String text = "Hello there, how are you?";
        try {
            User newUser = new User(nickname, new InetSocketAddress(0));
            DatagramSocket socket = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(),0));
            User parentUser =  new User("Parent",(InetSocketAddress) socket.getLocalSocketAddress());
            byte[] dataPacket = PacketHandler.constructParentPacket(newUser.getID(), parentUser);
            System.out.println(PacketHandler.getUserID(dataPacket));
            System.out.println(PacketHandler.getPacketType(dataPacket));
            System.out.println(PacketHandler.getSocketAddress(dataPacket));
        }
        catch ( UnknownHostException ex ) {
            ex.printStackTrace();
        }
        catch ( SocketException ex ) {
            ex.printStackTrace();
        }
    }
}

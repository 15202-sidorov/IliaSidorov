import org.omg.CORBA.TIMEOUT;

import javax.sound.sampled.Port;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.*;


class MulticastThread extends Thread {
    public void run() {
        try {
            InetAddress group_address = InetAddress.getByName(GROUP_MULTICAST_ADDRESS);
            MulticastSocket group_socket = new MulticastSocket(GROUP_PORT);
            HashMap<InetAddress, Integer> group_list = new HashMap<InetAddress, Integer>();
            byte[] message = new byte[256];
            String messageStr = null;
            int copies_amount = 0;

            //setting timeout and joining multicast group
            group_socket.setTimeToLive(TIMEOUT);
            group_socket.joinGroup(group_address);

            //configuring register datagram
            messageStr = REGISTER + " " + IP;
            message = messageStr.getBytes();
            DatagramPacket datagram = new DatagramPacket(message, message.length, InetAddress.getByName(GROUP_MULTICAST_ADDRESS), GROUP_PORT);

            //multicasting to the group the fact the we exist;
            group_socket.send(datagram);

            //making list of all others and waiting for someone to register
            while (true) {
                group_socket.receive(datagram);
                messageStr = datagram.getData().toString();
                String[] split = messageStr.split(" ");
                switch ( split[0] ) {
                    case "REGISTER":
                        if ( !group_list.containsKey(InetAddress.getByName(split[1])) ) {
                            System.out.println("New copy is found...");
                            group_list.put();
                            for ( Map.Entry<InetAddress, Integer > entry : group_list.entrySet() ) {
                                System.out.println("IP : " + entry.getKey().toString() + " Port : " +
                                    entry.getValue());
                            }
                        }
                        break;
                    case "OUT":
                        break;
                    default:
                        System.out.println("Could not recognize command " + split[0]);
                }
            }


        }
        catch ( UnknownHostException ex ) {
            System.out.println("Cannot find host");
        }
        catch ( IOException ex ) {
            System.out.println("IOException from socket");
        }
    }

    private final static String IP = "10.0.0.2";
    private final static String REGISTER = "REGISTER";
    private final static String OUT = "OUT";
    private final static String GROUP_MULTICAST_ADDRESS = "10.0.0.1";
    private final static int GROUP_PORT = 4255;
    private final static int TIMEOUT = 20;

}


public class MulticastServer {
    static public void main ( String args[] ) throws IOException {
        new MulticastThread().start();
    }
}


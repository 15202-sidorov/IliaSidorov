
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

class MulticastThread extends Thread {

    public void run() {

        try {
            IP = InetAddress.getLocalHost().getHostAddress();
            group_address = InetAddress.getByName(GROUP_MULTICAST_ADDRESS);
            group_socket = new MulticastSocket(GROUP_PORT);
            group_list = new HashMap<InetAddress , Integer>();
            message = new byte[256];
            messageStr = null;

            //setting timeout and joining multicast group
            group_socket.setTimeToLive(TIMEOUT);
            group_socket.joinGroup(group_address);

            sendRequest();

            //setting timeout for socket
            //that will make him check out whether the thread is interrupted or not
            //after 5 seconds
            group_socket.setSoTimeout(5000);

            //making list of all others and waiting for someone to register or the oposite
            while ( !this.isInterrupted() ) {
                try {
                    group_socket.receive(datagram);
                }
                catch ( SocketTimeoutException ex ) {
                    if ( this.isInterrupted() ) {
                        break;
                    }
                    else {
                        continue;
                    }
                }

                String[] split =  recieveMessage();
                switch ( split[0] ) {
                    case REGISTER_STRING:
                        if ( !group_list.containsKey( InetAddress.getByName(split[1]) )) {
                            copies_amount++;
                            System.out.println("New copy is found...");
                            group_list.put(InetAddress.getByName(split[1]), GROUP_PORT);
                            printWholeList();
                            //telling the new member that we do exist by sending request message once more
                            sendRequest();
                        }
                        break;
                    case OUT_STRING:
                        if ( group_list.containsKey(InetAddress.getByName(split[1])) ) {
                            copies_amount--;
                            System.out.println("Copy " +  split[1] + " is gone");
                            group_list.remove(InetAddress.getByName(split[1]));
                            printWholeList();
                        }
                        break;
                    default:
                        System.out.println("Could not recognize command " + split[0]);
                }
            }

            sendLeaveRequest();
            group_socket.leaveGroup(group_address);
            group_socket.close();

        }
        catch ( UnknownHostException ex ) {
            System.out.println("Cannot find host");
        }
        catch ( IOException ex ) {
            System.out.println("IOException from socket");
        }
        finally {
            if ( !group_socket.isClosed() ) {
                group_socket.close();
            }
        }
    }

    //configures the request datagram and sends it in multicast socket
    private void sendRequest() throws IOException, UnknownHostException {
        messageStr = REGISTER_STRING + " " + IP;
        message = messageStr.getBytes();
        datagram = new DatagramPacket(message, message.length, InetAddress.getByName(GROUP_MULTICAST_ADDRESS), GROUP_PORT);

        //multicasting to the group the fact the we exist;
        group_socket.send(datagram);

    }

    //configures out message to leave the group
    private void sendLeaveRequest() {
        messageStr = OUT_STRING + " " + IP;
        message = messageStr.getBytes();
        datagram.setData(message);
        try {
            group_socket.send(datagram);
        }
        catch ( IOException ex ) {
            System.out.println("Could not send OUT message");
        }
    }

    private String[] recieveMessage() {
        messageStr = new String(datagram.getData(), 0, datagram.getData().length);
        return messageStr.split(" ");
    }


    private void printWholeList() {
        for ( Map.Entry<InetAddress, Integer > entry : group_list.entrySet() ) {
            System.out.println("IP : " + entry.getKey().toString() + " Port : " +
                    entry.getValue());
        }
        System.out.println("Copies in the net : " + copies_amount);
    }

    private InetAddress group_address;
    private MulticastSocket group_socket;
    private byte[] message;
    private String messageStr;
    private DatagramPacket datagram;
    private HashMap<InetAddress, Integer> group_list;
    private int copies_amount = 0;

    private String IP;
    private final static String REGISTER_STRING = "REGISTER";
    private final static String OUT_STRING = "OUT";
    private final static String GROUP_MULTICAST_ADDRESS = "228.5.6.7";
    private final static int GROUP_PORT = 4255;
    private final static int TIMEOUT = 5;

}

public class MulticastServer {
    static public void main ( String args[] ) throws IOException {
        MulticastThread mainThread = new MulticastThread();
        mainThread.start();
        BufferedReader commands = new BufferedReader( new InputStreamReader(System.in) );
        while ( Objects.equals(commands.readLine(),EXIT_COMMAND) ) {

        }
        mainThread.interrupt();

        System.out.println("Exit success");
        try {
            mainThread.join();
        }
        catch ( InterruptedException ex ) {
            Thread.currentThread().interrupt();
        }
    }

    private final static String EXIT_COMMAND = "exit";
}


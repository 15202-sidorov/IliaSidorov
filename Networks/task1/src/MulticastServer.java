
import java.io.IOException;
import java.net.*;
import java.util.*;

class MainThread extends Thread {
    MainThread() {
        message = new byte[256];
    }

    public void run() {
        try {
            group_address = InetAddress.getByName("228.5.6.7");
            group_port = 3000;
            own_port = 3006;
            group_socket = new MulticastSocket(group_port);
            group_socket.joinGroup(group_address);
            String[] split;
            copy_list = new LinkedList<InetSocketAddress>();
            alive_list = new HashMap<InetSocketAddress, Boolean>();


            group_socket.setSoTimeout(TIMEOUT);
            sendPing();

            while ( !isInterrupted() ) {
                try {
                    split = receiveMessage();
                }
                catch ( SocketTimeoutException ex ) {
                    sendPing();
                    checkTimeout();
                    continue;
                }
                makeChanges(split);
            }


            group_socket.leaveGroup(group_address);
            group_socket.close();
        }
        catch (IOException ex) {
            System.out.println("Could not send ping");
        }
    }

    private void makeChanges( String[] split )  throws IOException{

        switch ( split[0] ) {
            case "PING":
                InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(split[1]), Integer.parseInt(split[2]));
                if (copy_list.contains(socketAddress)) {
                    alive_list.replace(socketAddress, true);
                } else {
                    copy_list.add(socketAddress);
                    alive_list.put(socketAddress, true);
                    printAllCopies();
                }
                break;
            default:
                System.out.println("Could not recongnize statement");
                break;

        }
    }

    private void checkTimeout() {
        Iterator<Map.Entry<InetSocketAddress, Boolean>> iter = alive_list.entrySet().iterator();
        while( iter.hasNext() ) {
            Map.Entry<InetSocketAddress, Boolean> item = iter.next();
            if ( !item.getValue() ) {
                copy_list.remove(item.getKey());
                iter.remove();
                printAllCopies();
            }
            else {
                item.setValue(false);
            }
        }
    }

    private void sendPing()  throws IOException {
        messageStr = "PING " + InetAddress.getLocalHost().getHostAddress() + " " + own_port;
        message = messageStr.getBytes();
        packet = new DatagramPacket(message, message.length, group_address, group_port);
        group_socket.send(packet);
    }

    private String[] receiveMessage() throws IOException, SocketTimeoutException {
        group_socket.receive(packet);


        return new String(packet.getData(),0,packet.getLength()).split(" ");
    }

    private void printAllCopies() {
        System.out.println("Some changes are made");
        Iterator<InetSocketAddress> iter = copy_list.iterator();
        while ( iter.hasNext() ) {
            InetSocketAddress item = iter.next();
            System.out.println("IP : " + item.getAddress() + " Port : " + item.getPort());
        }
    }


    private String messageStr;
    private byte[] message;
    private MulticastSocket group_socket;
    private InetAddress group_address;
    private int group_port;
    DatagramPacket packet;
    private int own_port;

    private LinkedList< InetSocketAddress > copy_list;
    private HashMap<InetSocketAddress, Boolean> alive_list;

    static private int TIMEOUT = 3000;

    static private String PING_STRING = "PING";

}


public class MulticastServer {
    static public void main( String[] args ) {
        MainThread thread = new MainThread();
        thread.start();
        try {
            thread.join();
        }
        catch ( InterruptedException ex ) {
            thread.interrupt();
        }

        return;
    }

}

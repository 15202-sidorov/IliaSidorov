/*
    Node of the chat tree
    Describes user who owns it, all siblings
    Also listens to all siblings and reacts on requests
    Current thread is also producing text on the screen if received
 */

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.SynchronousQueue;

//Blocks while no messages are passed
//receives all messages , modifies message queue if needed
//adds connections of the node
//disconnects some nodes
class ReceivingThread extends Thread {
    ReceivingThread (User inputOwner,
                     DatagramSocket socket,
                     DatagramPacket packet,
                     List<UUID> addr,
                     SynchronousQueue<String> mes ) throws ClassNotFoundException {
        owner = inputOwner;
        siblings = addr;
        clientPacket = packet;
        clientSocket = socket;
        messages = mes;
        if (!addr.getClass().equals(ArrayList.class)) {
            throw new ClassNotFoundException();
        }
    }

    public void run() {
        try {
            String stringToPrint;
            while( !Thread.currentThread().isInterrupted() ) {
                clientSocket.receive(clientPacket);
                switch (Packet.getPacketType(clientPacket.getData())) {
                    case PacketType.TEXT:
                        stringToPrint = Packet.getNickName(clientPacket.getData()) + ": " +
                                Packet.getText(clientPacket.getData());
                        messages.put(stringToPrint);
                        break;
                    case PacketType.CONNECT:
                        siblings.add(Packet.getUserID(clientPacket.getData()));
                        Packet.constructPacket(owner.getID(), PacketType.ACK);
                        break;
                    case PacketType.DISCONNECT:
                        siblings.remove(Packet.getUserID(clientPacket.getData()));
                        break;
                    case PacketType.PARENT:
                        break;
                    case PacketType.ROOT:
                        break;
                    default:
                        System.out.println("No such command");
                        break;

                }


            }
        }
        catch (IOException ex) {
            System.out.println("IOException in receiving thread");
        }
        catch ( InterruptedException ex ) {
            System.out.println("Receiving thread is interrupted");
            Thread.currentThread().interrupt();
        }

    }

    private DatagramSocket clientSocket;
    private DatagramPacket clientPacket;
    private List siblings;
    private SynchronousQueue<String> messages;
    private User owner;
}


//Looks out in message queue and if it is not empty
//prints the message
class TextOutThread extends Thread {
    TextOutThread ( SynchronousQueue<String> messages ) {
        queue = messages;
    }

    public void run() {
        try {
            while ( !Thread.currentThread().isInterrupted() ) {
                System.out.println(queue.take());
            }
        }
        catch ( InterruptedException ex ) {
            Thread.currentThread().interrupt();
        }

    }

    private SynchronousQueue<String> queue;
}

public class Node extends Thread {
    //used for root
    public Node( User inputOwner ) throws SocketException {
        owner = inputOwner;
        allChildren = Collections.synchronizedList( new ArrayList<InetSocketAddress>() );
        messages = new SynchronousQueue<String>();
        isRoot = true;
        //binds socket to a local address to register
        buffer = new byte[BUFFER_CAPACITY];
        serverSocket = new DatagramSocket(REGISTER_PORT);
        packetReceived = new DatagramPacket(buffer, buffer.length);

    }

    public Node (User inputOwner, User inputParent) throws SocketException {
        owner = inputOwner;
        allChildren = Collections.synchronizedList( new ArrayList<UUID>() );
        parent = inputParent;
        messages = new SynchronousQueue<String>();
        isRoot = false;
        //binds socket to a local address to register
        buffer = new byte[BUFFER_CAPACITY];
        serverSocket = new DatagramSocket(REGISTER_PORT);
        packetReceived = new DatagramPacket(buffer, buffer.length);
    }

    public void run() {
        try {
            //connect to the tree
            receiveT = new ReceivingThread(owner,serverSocket , packetReceived , allChildren, messages);
            receiveT.start();
            sendT = new TextOutThread(messages);
            sendT.start();
            String receivedMessage;
            Scanner in = new Scanner(System.in);
            while ( !Thread.currentThread().isInterrupted() ) {
                receivedMessage = in.nextLine();
                messages.put(receivedMessage);
            }

        }
        catch ( InterruptedException ex ) {
            System.out.println("Main thread is interrupted");
            receiveT.interrupt();
            Thread.currentThread().interrupt();
        }
        catch ( ClassNotFoundException ex ) {
            System.out.println(ex.getMessage());
            receiveT.interrupt();
        }
        finally {
            serverSocket.close();
        }

    }

    private void changeParent( User newParent ) {

    }

    private void addChild( User newChild ) {

    }

    private void sendText( String text ) {

    }

    private Thread receiveT;
    private Thread sendT;

    private byte[] buffer;
    private DatagramSocket serverSocket;
    private DatagramPacket packetReceived;

    private User owner;
    private User parent;
    private boolean isRoot;
    private List allChildren;
    private SynchronousQueue<String> messages;

    private static final int REGISTER_PORT = 3000;
    private static final int BUFFER_CAPACITY = 64;
}

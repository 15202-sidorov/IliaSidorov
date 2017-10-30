/*

    Node thread receives messages from stdin and prints it
    Also generates
        --receiving thread, (receives and handles messages)
        --sending thread, (sends messages to other nodes)
        --message thread. (handles all message output)

 */

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.SynchronousQueue;

public class Node extends Thread {
    //root user
    public Node(User nodeOwner) throws UnknownHostException, SocketException {
        socket = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(), 0));
        nodeOwner.setSocketAddress((InetSocketAddress) socket.getLocalSocketAddress());
        parentAddress = (InetSocketAddress) socket.getLocalSocketAddress();
        childAddress = Collections.synchronizedList(new ArrayList<InetSocketAddress>());
        siblingStatus = Collections.synchronizedMap(new HashMap<InetSocketAddress, SiblingStatus>());
        messages = new SynchronousQueue<String>();
        connectionHandler = new ConnectionHandler(nodeOwner, socket, siblingStatus);
        generateThreads();
        System.out.println("Root node is created, all threads started, current address is : " + nodeOwner.getAddress());
    }

    //child user
    public Node(User nodeOwner, InetSocketAddress inputParentAddress)
            throws UnknownHostException, SocketException, InterruptedException, IOException {
        socket = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(), 0));
        nodeOwner.setSocketAddress((InetSocketAddress) socket.getLocalSocketAddress());
        parentAddress = inputParentAddress;
        childAddress = Collections.synchronizedList(new ArrayList<InetSocketAddress>());
        siblingStatus = Collections.synchronizedMap(new HashMap<InetSocketAddress, SiblingStatus>());
        messages = new SynchronousQueue<String>();
        connectionHandler = new ConnectionHandler(nodeOwner, socket, siblingStatus);
        generateThreads();
        connectionHandler.sendCONNECT(parentAddress);
        System.out.println("Child node is created, all threads started, current address is : " + nodeOwner.getAddress());
    }

    private void generateThreads() {
        messageThread = new MessageOutThread(messages);
        messageThread.start();
        receivingThread = new ReceivingThread(socket, messages,
                                              siblingStatus,
                                              parentAddress,childAddress, connectionHandler);
        receivingThread.start();
        checkTimer = new Timer();
        checkTimer.scheduleAtFixedRate(new CheckTimerTask(siblingStatus,connectionHandler), TIMER_CHECK, TIMER_CHECK);
    }

    @Override
    public void run() {
        Scanner textScanner = new Scanner(System.in);
        String textEntered = null;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                textEntered = textScanner.nextLine();
                messages.put(textEntered);
                for ( InetSocketAddress address : siblingStatus.keySet() ) {
                    connectionHandler.sendTEXT(address, textEntered);
                }
            }
        }
        catch ( InterruptedException ex ) {
            Thread.currentThread().interrupt();
        }
        catch ( IOException ex ) {
            ex.printStackTrace();
        }
    }

    private Thread messageThread;
    private Thread receivingThread;
    private Timer checkTimer;


    private DatagramSocket socket;
    private InetSocketAddress parentAddress;
    private List<InetSocketAddress> childAddress;
    private Map<InetSocketAddress ,SiblingStatus> siblingStatus;

    private SynchronousQueue<String> messages;
    private ConnectionHandler connectionHandler;

    private final static int TIMER_CHECK = 1000;

}
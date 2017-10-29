/*

    Node thread receives messages from stdin and prints it
    Also generates
        --receiving thread, (receives and handles messages)
        --sending thread, (sends messages to other nodes)
        --message thread. (handles all message output)

 */

import java.net.*;
import java.util.*;
import java.util.concurrent.SynchronousQueue;

import static java.util.Collections.synchronizedList;

public class Node extends Thread {
    //root user
    public Node(User nodeOwner) throws UnknownHostException, SocketException {
        socket = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(), 0));
        nodeOwner.setSocketAddress((InetSocketAddress) socket.getLocalSocketAddress());
        parentAddress = (InetSocketAddress) socket.getLocalSocketAddress();
        childAddress = Collections.synchronizedList(new ArrayList<InetSocketAddress>());
        siblingStatus = Collections.synchronizedMap(new HashMap<InetSocketAddress, SiblingStatus>());
        messages = new SynchronousQueue<String>();
        packetQueue = new SynchronousQueue<DatagramPacket>();
        connectionHandler = new ConnectionHandler(nodeOwner, socket, packetQueue, (HashMap<InetSocketAddress, SiblingStatus>) siblingStatus);

        //generateThreads();
    }

    //child user
    public Node(User nodeOwner, InetSocketAddress inputParentAddress) throws UnknownHostException, SocketException {
        socket = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(), 0));
        nodeOwner.setSocketAddress((InetSocketAddress) socket.getLocalSocketAddress());
        parentAddress = inputParentAddress;
        childAddress = Collections.synchronizedList(new ArrayList<InetSocketAddress>());
        siblingStatus = Collections.synchronizedMap(new HashMap<InetSocketAddress, SiblingStatus>());
        messages = new SynchronousQueue<String>();
        packetQueue = new SynchronousQueue<DatagramPacket>();
        connectionHandler = new ConnectionHandler(nodeOwner, socket, packetQueue, (HashMap<InetSocketAddress, SiblingStatus>) siblingStatus);
    }

    private void generateThreads() {
        messageThread = new MessageOutThread(messages);
        messageThread.start();
        receivingThread = new ReceivingThread(socket, packetQueue, messages,
                                             (HashMap<InetSocketAddress, SiblingStatus>) siblingStatus,
                                              parentAddress,childAddress, connectionHandler);
        receivingThread.start();
        checkTimer = new Timer();


    }

    @Override
    public void run() {

    }

    private Thread messageThread;
    private Thread receivingThread;
    private Thread sendingThread;
    private Timer checkTimer;


    private DatagramSocket socket;
    private InetSocketAddress parentAddress;
    private List<InetSocketAddress> childAddress;
    private Map<InetSocketAddress ,SiblingStatus> siblingStatus;

    private SynchronousQueue<String> messages;
    private SynchronousQueue<DatagramPacket> packetQueue;
    private ConnectionHandler connectionHandler;

}
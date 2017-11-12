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
        owner = nodeOwner;
        socket = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(), 0));
        nodeOwner.setSocketAddress((InetSocketAddress) socket.getLocalSocketAddress());
        parentAddress = (InetSocketAddress) socket.getLocalSocketAddress();
        //childAddress = Collections.synchronizedList(new ArrayList<InetSocketAddress>());
        siblingStatus = Collections.synchronizedMap(new HashMap<InetSocketAddress, SiblingStatus>());
        messages = new SynchronousQueue<String>();
        connectionHandler = new ConnectionHandler(nodeOwner, socket, siblingStatus);
        generateThreads();
        System.out.println("Root node is created, all threads started, current address is : " + nodeOwner.getAddress());
    }

    //child user
    public Node(User nodeOwner, InetSocketAddress inputParentAddress)
            throws UnknownHostException, SocketException, InterruptedException, IOException {
        owner = nodeOwner;
        socket = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(), 0));
        nodeOwner.setSocketAddress((InetSocketAddress) socket.getLocalSocketAddress());
        parentAddress = inputParentAddress;
        //childAddress = Collections.synchronizedList(new ArrayList<InetSocketAddress>());
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
                                              parentAddress,/*childAddress,*/ connectionHandler);
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
                if ( "QUID".equals(textEntered) ) {
                    shutdownFunction();
                    return;
                }

                for ( InetSocketAddress address : siblingStatus.keySet() ) {
                    connectionHandler.sendTEXT(address, textEntered);
                }
            }
            receivingThread.interrupt();
            messageThread.interrupt();
            checkTimer.cancel();

        }

        catch ( InterruptedException ex ) {
            receivingThread.interrupt();
            messageThread.interrupt();
            checkTimer.cancel();
            Thread.currentThread().interrupt();

        }
        catch ( IOException ex ) {
            ex.printStackTrace();
        }
    }

    private void shutdownFunction() throws IOException, InterruptedException {
        System.out.println("SHUTDOWN, trying to disconnect manually ...");
        receivingThread.interrupt();
        messageThread.interrupt();
        checkTimer.cancel();
        InetSocketAddress chosedChild = null;
        //in case node is root, need to choose new root and connect others to them
        if ( parentAddress.equals(owner.getAddress()) ) {
            if ( !siblingStatus.isEmpty() ) {
                for ( InetSocketAddress address : siblingStatus.keySet() ) {
                    if ( siblingStatus.get(address).isAvailable() ) {
                        chosedChild = address;
                        continue;
                    }
                }

                System.out.println("Send root");
                connectionHandler.sendPACKET(chosedChild, PacketType.ROOT);
                for ( InetSocketAddress address : siblingStatus.keySet() ) {
                    if ( !address.equals(chosedChild) ) {
                        connectionHandler.sendPARENT(address, chosedChild);
                    }
                }
            }

            return;
        }

        System.out.println("Sending new parent to all children");
        for (InetSocketAddress address : siblingStatus.keySet()) {
            if ( !address.equals(parentAddress) ) {
                connectionHandler.sendPARENT(parentAddress, address);
            }
        }
    }

    private Thread messageThread;
    private Thread receivingThread;
    private Timer checkTimer;

    private User owner;
    private DatagramSocket socket;
    private InetSocketAddress parentAddress;
    //private List<InetSocketAddress> childAddress;
    private Map<InetSocketAddress ,SiblingStatus> siblingStatus;

    private SynchronousQueue<String> messages;
    private ConnectionHandler connectionHandler;

    private final static int TIMER_CHECK = 1000;

}
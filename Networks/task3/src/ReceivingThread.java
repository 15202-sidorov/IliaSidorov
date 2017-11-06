/*

    Receiving thread receives all the messages coming in to the node,
    and handles them.

 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

public class ReceivingThread extends Thread {
    public ReceivingThread(DatagramSocket inputNodeSocket,
                           SynchronousQueue<String> inputMessageQueue,
                           Map<InetSocketAddress, SiblingStatus> inputSiblingsStatus,
                           InetSocketAddress inputParentAddress,
                           List<InetSocketAddress> inputChildAddress,
                           ConnectionHandler inputConnectionHandler) {
        nodeSocket = inputNodeSocket;
        messageQueue = inputMessageQueue;
        siblingStatus = inputSiblingsStatus;
        parentAddress = inputParentAddress;
        childAddress = inputChildAddress;
        connectionHandler = inputConnectionHandler;
        System.out.println("Receiving thread is initialized");
    }

    @Override
    public void run() {
        try {
            while ( !Thread.currentThread().isInterrupted() ) {
                DatagramPacket receivedPacket = connectionHandler.receivePacket();
                if ( receivedPacket == null ) {
                    System.out.println("Dropping...");
                    continue;
                }

                switch (PacketHandler.getPacketType(receivedPacket.getData())) {
                    case PacketType.CONNECT:
                        handleCONNECT(receivedPacket);
                        break;
                    case PacketType.DISCONNECT:
                        handleDISCONNECT(receivedPacket);
                        break;
                    case PacketType.ACK:
                        handleACK(receivedPacket);
                        break;
                    case PacketType.TEXT:
                        handleTEXT(receivedPacket);
                        break;
                    case PacketType.ROOT:
                        handleROOT(receivedPacket);
                        break;
                    case PacketType.PARENT:
                        handlePARENT(receivedPacket);
                        break;
                }
            }
        }
        catch ( IOException ex ) {
            ex.printStackTrace();
        }
        catch ( InterruptedException ex ) {
            Thread.currentThread().interrupt();
        }
    }

    //call after message is received

    // A connect request has come from a child or parent.
    // The same as PING refresh status and if there is no child connected, add one.
    private void handleCONNECT( DatagramPacket receivedPacket ) throws InterruptedException, IOException {
        InetSocketAddress receivedFrom = (InetSocketAddress) receivedPacket.getSocketAddress();
        //System.out.println("Handling CONNECT from " + receivedFrom);
        if ( !siblingStatus.containsKey(receivedFrom) ) {
            siblingStatus.put(receivedFrom, new SiblingStatus(receivedFrom));
            System.out.println(receivedFrom + " has connected");
            if (receivedFrom != parentAddress) {
                childAddress.add(receivedFrom);
            }
        }
        connectionHandler.sendPACKET(receivedFrom, PacketType.ACK);
    }

    //disconnect request from child
    private void handleDISCONNECT( DatagramPacket receivedPacket ) throws InterruptedException, IOException {
        InetSocketAddress receivedFrom = (InetSocketAddress) receivedPacket.getSocketAddress();
        if ( siblingStatus.containsKey(receivedFrom) ) {
            siblingStatus.remove(receivedFrom);
            if ( !receivedFrom.equals(parentAddress) ) {
                childAddress.remove(receivedFrom);
            }
            else if ( receivedFrom.equals(parentAddress) ) {
                //if root disconnected without previously sending an ROOT or PARENT request
                parentAddress = (InetSocketAddress) nodeSocket.getLocalSocketAddress();
            }
            System.out.println(receivedFrom + " disconnected manually");
        }
    }

    private void handleACK( DatagramPacket receivedPacket ) throws InterruptedException {
        InetSocketAddress receivedFrom = (InetSocketAddress) receivedPacket.getSocketAddress();
        //System.out.println("Handling ACK from " + receivedFrom);
        if ( siblingStatus.containsKey(receivedFrom) ) {
            siblingStatus.get(receivedFrom).gotAck();
        }
    }

    private void handleROOT ( DatagramPacket receivedPacket ) throws InterruptedException, IOException {
        InetSocketAddress receivedFrom = (InetSocketAddress) receivedPacket.getSocketAddress();
       // System.out.println("Handling ROOT from " + receivedFrom);
        connectionHandler.sendPACKET(parentAddress, PacketType.DISCONNECT);
        parentAddress = (InetSocketAddress) nodeSocket.getLocalSocketAddress();
    }

    private void handleTEXT( DatagramPacket receivedPacket ) throws IOException, InterruptedException {
        InetSocketAddress receivedFrom = (InetSocketAddress) receivedPacket.getSocketAddress();
        //System.out.println("Handling TEXT from " + receivedFrom);
        if ( siblingStatus.containsKey(receivedFrom) ) {
            byte[] packetData = receivedPacket.getData();
            String textReceived = PacketHandler.getNickName(packetData) + ": " + PacketHandler.getText(packetData);
            messageQueue.put(textReceived);

            for ( InetSocketAddress address : siblingStatus.keySet() ) {
                if ( !receivedFrom.equals(address) ) {
                    //System.out.println("RESEND");
                    connectionHandler.sendTEXT(address, textReceived);
                }
            }
        }
    }

    private void handlePARENT( DatagramPacket receivedPacket )
            throws UnknownHostException, InterruptedException, IOException {
        InetSocketAddress receivedFrom = (InetSocketAddress) receivedPacket.getSocketAddress();
        //System.out.println("Handling NEW PARENT from " + receivedFrom);
        if ( parentAddress.equals(receivedFrom) ) {
            parentAddress = PacketHandler.getSocketAddress(receivedPacket.getData());
            connectionHandler.sendPACKET(parentAddress, PacketType.CONNECT);
        }
    }

    private DatagramSocket nodeSocket;
    private SynchronousQueue<String> messageQueue;
    private Map<InetSocketAddress, SiblingStatus> siblingStatus;
    private InetSocketAddress parentAddress;
    private List<InetSocketAddress> childAddress;
    private ConnectionHandler connectionHandler;
}

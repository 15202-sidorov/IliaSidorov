/*

    Receiving thread receives all the messages coming in to the node,
    and handles them.

 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

public class ReceivingThread extends Thread {
    public ReceivingThread(DatagramSocket inputNodeSocket,
                           SynchronousQueue<String> inputMessageQueue,
                           HashMap<InetSocketAddress, SiblingStatus> inputSiblingsStatus,
                           InetSocketAddress inputParentAddress,
                           List<InetSocketAddress> inputChildAddress,
                           ConnectionHandler inputConnectionHandler) {
        nodeSocket = inputNodeSocket;
        messageQueue = inputMessageQueue;
        siblingStatus = inputSiblingsStatus;
        parentAddress = inputParentAddress;
        childAddress = inputChildAddress;
        connectionHandler = inputConnectionHandler;
    }

    @Override
    public void run() {
        try {
            while ( !Thread.currentThread().isInterrupted() ) {
                DatagramPacket receivedPacket = connectionHandler.receivePacket();
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

        if ( !siblingStatus.containsKey(receivedFrom) ) {
            siblingStatus.put(receivedFrom, new SiblingStatus(receivedFrom));
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
            connectionHandler.sendPACKET(receivedFrom, PacketType.ACK);
        }
    }

    private void handleACK( DatagramPacket receivedPacket ) {
        InetSocketAddress receivedFrom = (InetSocketAddress) receivedPacket.getSocketAddress();
        if ( siblingStatus.containsKey(receivedFrom) ) {
            siblingStatus.get(receivedFrom).gotAck();
        }
    }

    private void handleROOT ( DatagramPacket receivedPacket ) throws InterruptedException, IOException {
        InetSocketAddress receivedFrom = (InetSocketAddress) receivedPacket.getSocketAddress();
        connectionHandler.sendPACKET(parentAddress, PacketType.DISCONNECT);
        parentAddress = (InetSocketAddress) nodeSocket.getLocalSocketAddress();
        connectionHandler.sendPACKET(receivedFrom, PacketType.ACK);
    }

    private void handleTEXT( DatagramPacket receivedPacket ) throws IOException, InterruptedException {
        InetSocketAddress receivedFrom = (InetSocketAddress) receivedPacket.getSocketAddress();
        if ( siblingStatus.containsKey(receivedFrom) ) {
            byte[] packetData = receivedPacket.getData();
            String textReceived = PacketHandler.getNickName(packetData) + ": " + PacketHandler.getText(packetData);
            messageQueue.put(textReceived);
            connectionHandler.sendPACKET(receivedFrom, PacketType.ACK);

            for (int i = 0; i < childAddress.size(); i++) {
                if ( !receivedFrom.equals(childAddress.get(i)) ) {
                    connectionHandler.sendTEXT(childAddress.get(i), textReceived);
                }
            }

            if ( !receivedFrom.equals(parentAddress) && !parentAddress.equals(nodeSocket.getLocalSocketAddress())) {
                connectionHandler.sendTEXT(parentAddress, textReceived);
            }
        }
    }

    private void handlePARENT( DatagramPacket receivedPacket )
            throws UnknownHostException, InterruptedException, IOException {
        InetSocketAddress receivedFrom = (InetSocketAddress) receivedPacket.getSocketAddress();
        if ( parentAddress.equals(receivedFrom) ) {
            connectionHandler.sendPACKET(receivedFrom, PacketType.ACK);
            parentAddress = PacketHandler.getSocketAddress(receivedPacket.getData());
            connectionHandler.sendPACKET(parentAddress, PacketType.CONNECT);
        }
    }

    private DatagramSocket nodeSocket;
    private SynchronousQueue<String> messageQueue;
    private HashMap<InetSocketAddress, SiblingStatus> siblingStatus;
    private InetSocketAddress parentAddress;
    private List<InetSocketAddress> childAddress;
    private ConnectionHandler connectionHandler;
}

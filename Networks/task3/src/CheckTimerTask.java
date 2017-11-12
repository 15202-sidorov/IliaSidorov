/*
    Timer timer task sets the scheduled timer in Node class.
    It checks all statuses of registered connections.
    If there is no ping from some of them for a long period of time
        the connection is deleted from registered connections list.
    If there is no ack from some node, the message waiting for ack to be received
        is send once again.
    If any of those conditions are true, node is no allowed to receive any messages,
        and is regarded to be in unavailable state.
    All messages coming to an unavailable node are put in the queue and actually send via socket,
        when its state becomes available.
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.TimerTask;

public class CheckTimerTask extends TimerTask {
    public CheckTimerTask(Map<InetSocketAddress, SiblingStatus> inputSiblingsStatus,
                          ConnectionHandler inputConnectionHandler) {
        siblingsStatus = inputSiblingsStatus;
        connectionHandler = inputConnectionHandler;
    }

    @Override
    public void run() {
        try {
            //System.out.println("Check is on ");
            for (InetSocketAddress address : siblingsStatus.keySet()) {
                SiblingStatus currentSibling = siblingsStatus.get(address);
                //System.out.println( "QUEUE SIZE : " + currentSibling.getQueueSize() );
                if ( !currentSibling.packetQueueIsEmpty() ) {
                    if ( !currentSibling.isAvailable() ) {
                        if ( !currentSibling.getPingStatus() ) {
                           // System.out.println("REMOVING");
                            System.out.println(address + " disconnected");
                            siblingsStatus.remove(address);
                            continue;
                        } else if ( !currentSibling.getAckStatus() ) {
                            //System.out.println("NO ACK");
                            DatagramPacket packetDropped = siblingsStatus.get(address).pullFromPacketQueue();
                            connectionHandler.sendPACKET_PUSH(packetDropped);
                        }
                    } else {
                        if ( !currentSibling.packetQueueIsEmpty() ) {
                            DatagramPacket packetToResend = currentSibling.pullFromPacketQueue();
                            connectionHandler.sendPACKET(packetToResend);
                            return;
                        }
                    }
                }

                connectionHandler.sendCONNECT(address);
                siblingsStatus.get(address).noPing();
            }
        }
        catch ( InterruptedException ex ) {
            Thread.currentThread().interrupt();
            System.out.println("interrupt");
        }
        catch ( IOException ex ) {
            ex.printStackTrace();
        }

    }

    private Map<InetSocketAddress, SiblingStatus> siblingsStatus;
    private ConnectionHandler connectionHandler;
}

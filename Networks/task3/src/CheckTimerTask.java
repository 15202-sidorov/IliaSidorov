import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.SynchronousQueue;

//!! put all messages to queue (those which are sent already and are intended to send)

public class CheckTimerTask extends TimerTask {
    public CheckTimerTask(Map<InetSocketAddress, SiblingStatus> inputSiblingsStatus,
                          ConnectionHandler inputConnectionHandler) {
        siblingsStatus = inputSiblingsStatus;
        connectionHandler = inputConnectionHandler;
    }

    @Override
    public void run() {
        try {
            System.out.println("Check is on");
            for (InetSocketAddress address : siblingsStatus.keySet()) {
                SiblingStatus currentSibling = siblingsStatus.get(address);
                if ( !currentSibling.packetQueueIsEmpty() ) {
                    if ( !currentSibling.isAvailable() ) {
                        if ( !currentSibling.getPingStatus() ) {
                            siblingsStatus.remove(address);
                        } else if ( !currentSibling.getAckStatus() ) {
                            siblingsStatus.remove(address);
                        } else {
                            currentSibling.noPing();
                            currentSibling.noAck();
                        }
                    } else {
                        currentSibling.pullFromPacketQueue();
                        if ( !currentSibling.packetQueueIsEmpty() ) {
                            DatagramPacket packetToResend = currentSibling.pullFromPacketQueue();
                            connectionHandler.sendPACKET(packetToResend);
                        }
                    }
                } else {
                    connectionHandler.sendPACKET(currentSibling.getAddress(), PacketType.CONNECT);
                    currentSibling.noPing();
                }
            }
        }
        catch ( InterruptedException ex ) {
            Thread.currentThread().isInterrupted();
            System.out.println("interrupt");
        }
        catch ( IOException ex ) {
            ex.printStackTrace();
        }

    }

    private Map<InetSocketAddress, SiblingStatus> siblingsStatus;
    private ConnectionHandler connectionHandler;
}

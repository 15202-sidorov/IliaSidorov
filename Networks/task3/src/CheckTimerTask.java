import javax.xml.crypto.Data;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.concurrent.SynchronousQueue;

//!! put all messages to queue (those which are sent already and are intended to send)

public class CheckTimerTask extends TimerTask {
    public CheckTimerTask(SynchronousQueue<DatagramPacket> inputPacketQueue,
                          HashMap<InetSocketAddress, SiblingStatus> inputSiblingsStatus,
                          ConnectionHandler inputConnectionHandler) {
        packetQueue = inputPacketQueue;
        siblingsStatus = inputSiblingsStatus;
        connectionHandler = inputConnectionHandler;
    }

    @Override
    public void run() {
        DatagramPacket[] array = (DatagramPacket[]) packetQueue.toArray();

    }

    private SynchronousQueue<DatagramPacket> packetQueue;
    private HashMap<InetSocketAddress, SiblingStatus> siblingsStatus;
    private ConnectionHandler connectionHandler;
}

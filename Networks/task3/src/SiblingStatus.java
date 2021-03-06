/*

    Sibling status describes the status of registered connection of the node.
    It defined whether node is available or not. (see CheckTimerTask.java)
 */



import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SiblingStatus {
    public SiblingStatus( InetSocketAddress inputAddress) {
        address = inputAddress;
        aliveStatus = 0;
        waitForAckStatus = 0;
        packetQueue = new ConcurrentLinkedQueue<DatagramPacket>();
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void noPing() {
        //System.out.println("Waiting for ping " + aliveStatus + " from " + address);
        aliveStatus++;
    }

    public void noAck() {
       // System.out.println("Waiting for ack " + waitForAckStatus + " from " + address);
        waitForAckStatus++;
    }

    public void gotPing() {
        //System.out.println("Ping for " + address + " is received");
        //System.out.println("Size : " + packetQueue.size());
        aliveStatus = 0;
    }

    public void gotAck() throws InterruptedException {
        //System.out.println("Ack for " + address + " is received");
        //System.out.println("Size : " + packetQueue.size());
        waitForAckStatus = 0;
        packetQueue.poll();
    }

    public boolean getPingStatus() {
        return aliveStatus < CRITICAL_NO_PING_VALUE;
    }

    public boolean getAckStatus() {
        return waitForAckStatus < CRITICAL_NO_ACK_VALUE;
    }

    public boolean isAvailable() {
       // System.out.println("Checking out availability of " + address);
        return ((aliveStatus == 0) && (waitForAckStatus == 0));
    }

    public void pushToPacketQueue( DatagramPacket packet ) throws InterruptedException {
      //  System.out.println("Pushing packet to queue of " + address);
        packetQueue.offer(packet);
    }

    public DatagramPacket pullFromPacketQueue() throws InterruptedException {
       // System.out.println("Pulling packet from queue of " + address);
        return packetQueue.poll();
    }

    public boolean packetQueueIsEmpty() {
        //System.out.println("Packet queue of " + address + " is " + packetQueue.isEmpty());
        return packetQueue.isEmpty();
    }

    private InetSocketAddress address;
    private Integer aliveStatus;
    private Integer waitForAckStatus;
    private ConcurrentLinkedQueue<DatagramPacket> packetQueue;

    private final static Integer CRITICAL_NO_ACK_VALUE = 1;
    private final static Integer CRITICAL_NO_PING_VALUE = 4;
}

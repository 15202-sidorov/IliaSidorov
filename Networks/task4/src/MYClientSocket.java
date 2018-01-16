
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public abstract class MYClientSocket {
    public MYClientSocket( DatagramSocket serverUDPSocket,
                           ConnectionStatus inputStatus ) {
        currentStatus = inputStatus;
        UDPSocket = serverUDPSocket;
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        pingTimer = new Timer();
        pingTimer.scheduleAtFixedRate(new checkAliveTask(),5000,5000);
    }

    private class checkAliveTask extends TimerTask {
        public void run() {
            try {
                if ((currentStatus.isAlive) || (currentStatus.getStatus() == Status.LISTEN)) {
                    if (currentStatus.getStatus() == Status.LISTEN) {
                        try {
                            if (!currentStatus.queueIsEmpty()) {
                                DatagramPacket cancelPacket = currentStatus.pollPacket();
                                if (PacketConstructor.getFlag(cancelPacket.getData()) == Flags.FIN_FLAG) {
                                    System.out.println("Connection is closed by other side");
                                    closeSession();
                                    currentStatus.setStatus(Status.CLOSED);
                                    cancel();
                                }
                            }
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            return;
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    currentStatus.isAlive = false;
                } else if (currentStatus.getStatus() == Status.ESTABLISHED) {
                    UDPSocket.send(packetToSend);
                } else {
                    resetConnection();
                    currentStatus.setStatus(Status.CLOSED);
                    cancel();
                }
            }
            catch ( IOException ex ) {
                ex.printStackTrace();
            }
        }
    }

    public void send(byte[] dataToSend) throws IOException, InterruptedException {
        if ( currentStatus.getStatus() == Status.LISTEN ) {
            establishSession();
        }

        if ( currentStatus.getStatus() != Status.ESTABLISHED ) {
            System.out.println("Connection is not established yet");
            return;
        }

        int bytesHandled = 0;
        ByteBuffer tmpBuffer = null;
        byte[] tmpBufferArray = null;
        int sequenceNumber = 0;
        while ( bytesHandled < dataToSend.length ) {
            if ( bytesHandled + CHUNK_SIZE < dataToSend.length ) {
                tmpBuffer = ByteBuffer.wrap(dataToSend, bytesHandled, CHUNK_SIZE);
                tmpBufferArray = new byte[CHUNK_SIZE];
            }
            else {
                tmpBuffer = ByteBuffer.wrap(dataToSend, bytesHandled, dataToSend.length - bytesHandled);
                tmpBufferArray = new byte[dataToSend.length - bytesHandled];
            }

            tmpBuffer.get(tmpBufferArray);

            System.out.println("Sending " + tmpBufferArray.length + " bytes");
            byte[] data = PacketConstructor.buildDEFAULT(sequenceNumber, tmpBufferArray);
            packetToSend = new DatagramPacket(data, data.length);
            packetToSend.setSocketAddress(currentStatus.getAddress());
            UDPSocket.send(packetToSend);
            System.out.println("Chunk is send, waiting for ack...");
            DatagramPacket packetReceived = null;
            while (true) {
                packetReceived = currentStatus.pollPacket();
                if (PacketConstructor.getFlag(packetReceived.getData()) == Flags.RST_FLAG) {
                    System.out.println("RESET");
                    currentStatus.setStatus(Status.CLOSED);
                    return;
                }
                if (PacketConstructor.getFlag(packetReceived.getData()) == Flags.ACK_FLAG) {
                    break;
                }
            }
            bytesHandled += tmpBufferArray.length;
            sequenceNumber++;
        }

        closeSession();
    }

    public byte[] receive() throws IOException,InterruptedException {
        if ( currentStatus.getStatus() != Status.LISTEN ) {
            System.out.println("Wrong socket condition");
            return null;
        }

        DatagramPacket packetReceived = currentStatus.pollPacket();
        if ( PacketConstructor.getFlag(packetReceived.getData()) == Flags.SYN_FLAG ) {
            currentStatus.setStatus(Status.SYN_RECEIVED);
            establishSession();
        }

        if ( currentStatus.getStatus() != Status.ESTABLISHED ) {
            System.out.println("Connection is not established yet");
            return null;
        }

        int bytesReceived = 0;
        int sequenceNumber = 0;
        while ( true ) {
            if ( bytesReceived > BUFFER_SIZE ) {
                System.out.println("BUFFER OVERFLOW");
                resetConnection();
                currentStatus.setStatus(Status.CLOSED);
                return null;
            }
            System.out.println("Waiting for chunk");
            packetReceived = currentStatus.pollPacket();
            if ( PacketConstructor.getFlag(packetReceived.getData()) == Flags.FIN_FLAG ) {
                currentStatus.setStatus(Status.FIN_RECEIVED);
                closeSession();
                break;
            }

            if ( PacketConstructor.getFlag(packetReceived.getData()) == Flags.RST_FLAG) {
                System.out.println("RESET");
                currentStatus.setStatus(Status.CLOSED);
                return null;
            }
            int size = PacketConstructor.getDataSize(packetReceived.getData());
            if  ( sequenceNumber != PacketConstructor.getSeq(packetReceived.getData()) ) {
                System.out.println("Wrong sequence number");
                currentStatus.packetReceived(packetReceived);
                continue;
            }

            byte[] content = PacketConstructor.getData(packetReceived.getData());
            buffer.position(bytesReceived);
            buffer.put(content);

            bytesReceived += size;
            System.out.println("Bytes received : " + bytesReceived);
            byte[] dataInPacket = PacketConstructor.buildACK(sequenceNumber,bytesReceived,BUFFER_SIZE - bytesReceived);
            packetReceived = new DatagramPacket(dataInPacket, dataInPacket.length);
            packetReceived.setSocketAddress(currentStatus.getAddress());
            UDPSocket.send(packetReceived);
            System.out.println("Ack is send");
            sequenceNumber++;
        }

        byte[] returnBytes = new byte[bytesReceived];
        buffer.rewind();
        buffer.get(returnBytes,0,bytesReceived);

        return returnBytes;
    }

    public void close() throws IOException, InterruptedException {
        pingTimer.cancel();
        currentStatus.setStatus(Status.CLOSING);
        System.out.println("Closing connection...");
        byte[] packetData = PacketConstructor.buildFIN(false);
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length);
        packet.setSocketAddress(currentStatus.getAddress());
        UDPSocket.send(packet);
        System.out.println("Waiting for ack...");
        while(true) {
            packet = currentStatus.pollPacket();
            if (PacketConstructor.getFlag(packet.getData()) == (Flags.FIN_FLAG | Flags.ACK_FLAG)) {
                packetData = PacketConstructor.buildFIN(true);
                packet = new DatagramPacket(packetData, packetData.length);
                packet.setSocketAddress(currentStatus.getAddress());
                UDPSocket.send(packet);
                break;
            }

            if (PacketConstructor.getFlag(packet.getData())  == (Flags.RST_FLAG)) {
                System.out.println("RESET");
                currentStatus.setStatus(Status.CLOSED);
                return;
            }
        }
        System.out.println("Client socket is closed");
    }

    protected void establishSession() throws IOException,InterruptedException {
        System.out.println("Establishing session");
        if ( currentStatus.getStatus() == Status.LISTEN ) {
            byte[] synData = PacketConstructor.buildSYN(false);
            DatagramPacket packet = new DatagramPacket(synData, synData.length);
            packet.setSocketAddress(currentStatus.getAddress());
            UDPSocket.send(packet);
            currentStatus.setStatus(Status.SYN_SENT);
            DatagramPacket packetReceived = null;
            while (true) {
                packetReceived = currentStatus.pollPacket();
                if ( PacketConstructor.getFlag(packetReceived.getData()) == (Flags.SYN_FLAG | Flags.ACK_FLAG) ) {
                    System.out.println("Connection is established successfully");
                    currentStatus.setStatus(Status.ESTABLISHED);
                    break;
                }

                if ( PacketConstructor.getFlag(packetReceived.getData()) == Flags.RST_FLAG ){
                    System.out.println("RESET");
                    currentStatus.setStatus(Status.CLOSED);
                }
            }

        }
        else if ( currentStatus.getStatus() == Status.SYN_RECEIVED ) {
            byte[] synAckPacketData = PacketConstructor.buildSYN( true);
            DatagramPacket synAckPacket = new DatagramPacket(synAckPacketData, synAckPacketData.length);
            synAckPacket.setSocketAddress(currentStatus.getAddress());
            UDPSocket.send(synAckPacket);
            System.out.println("Session is established successfully");
            currentStatus.setStatus(Status.ESTABLISHED);
        }
        else {
            System.out.println("SESSION IS NOT ESTABLISHED");
        }
    }

    protected void closeSession() throws IOException, InterruptedException {
        System.out.println("Closing session");
        if ( currentStatus.getStatus() == Status.ESTABLISHED ) {
            byte[] packetData = PacketConstructor.buildFIN(false);
            DatagramPacket packet = new DatagramPacket(packetData, packetData.length);
            packet.setSocketAddress(currentStatus.getAddress());
            UDPSocket.send(packet);
            currentStatus.setStatus(Status.FIN_WAIT);
            while(true) {
                packet = currentStatus.pollPacket();
                if (PacketConstructor.getFlag(packet.getData()) == (Flags.FIN_FLAG | Flags.ACK_FLAG)) {
                    System.out.println("Session is closed, ending session");
                    packetData = PacketConstructor.buildFIN(true);
                    packet.setData(packetData);
                    packet.setSocketAddress(currentStatus.getAddress());
                    UDPSocket.send(packet);
                    currentStatus.setStatus(Status.LISTEN);
                    break;
                }

                if (PacketConstructor.getFlag(packet.getData()) == Flags.RST_FLAG) {
                    System.out.println("RESET");
                    currentStatus.setStatus(Status.CLOSED);
                    return;
                }
            }
        }
        else if (( currentStatus.getStatus() == Status.FIN_RECEIVED ) || (currentStatus.getStatus() == Status.LISTEN)) {
            byte[] packetData = PacketConstructor.buildFIN(true);
            DatagramPacket packet = new DatagramPacket(packetData, packetData.length);
            packet.setSocketAddress(currentStatus.getAddress());
            UDPSocket.send(packet);
            currentStatus.setStatus(Status.CLOSING);
            while (true) {
                packet = currentStatus.pollPacket();
                if (PacketConstructor.getFlag(packet.getData()) == (Flags.FIN_FLAG | Flags.ACK_FLAG)) {
                    currentStatus.setStatus(Status.LISTEN);
                    System.out.println("Session is closed");
                    break;
                }

                if (PacketConstructor.getFlag(packet.getData()) == (Flags.RST_FLAG)) {

                }
            }
        }
        else {
            System.out.println("Unable to close session");
            return;
        }


    }

    protected void resetConnection() throws IOException {
        byte[] packetData = PacketConstructor.buildRST();
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length);
        packet.setSocketAddress(currentStatus.getAddress());
        UDPSocket.send(packet);
        System.out.println("Connection is reset");
        currentStatus.setStatus(Status.CLOSED);
    }

    protected ConnectionStatus currentStatus;
    protected DatagramSocket UDPSocket;
    protected Timer pingTimer;
    protected DatagramPacket packetToSend;

    protected ByteBuffer buffer;
    protected static final short BUFFER_SIZE = 1024;
    protected static final short CHUNK_SIZE = 16;




}

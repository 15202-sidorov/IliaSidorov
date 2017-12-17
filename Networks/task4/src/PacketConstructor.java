/*

    PacketConstructor builds packet
    Each packet consists:

    -------------------------------
    |  FLAG   |   SEQ   |   ACK   |
    |-----------------------------|
    |   MT    |  SIZE   |  DATA   |
    -------------------------------

    FLAG -- Points out what kind of packet it is.
            -- SYN (for connection state)
            -- ACK (used for any ack) ACK field is used
            -- RST (in case something is wrong)
            -- FIN (for closing connection)

    SEQ  -- Sequence number.

    ACK  -- How many bytes received ( in case ACK is set ).

    MT   -- The size of receivers BUFFER. ( how many bytes server is ready to receive )

    SIZE -- Size of data in packet.

    DATA -- Target data.

*/

import java.nio.ByteBuffer;

public class PacketConstructor {

    public static byte[] buildACK( int sequenceNumber , int bytesReceived, int bufferSpaceAvailable ) {
        byte[] result = new byte[FLAGS_SIZE + SEQUENCE_NUMBER_SIZE + ACK_NUMBER_SIZE + MT];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer.putShort(Flags.ACK_FLAG);
        buffer.putInt(sequenceNumber);
        buffer.putInt(bytesReceived);
        buffer.putInt(bufferSpaceAvailable);
        return result;
    }

    public static byte[] buildSYN( boolean isAck ) {
        byte[] result = new byte[FLAGS_SIZE];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        if ( !isAck ) {
            buffer.putShort(Flags.SYN_FLAG);
        }
        else {
            buffer.putShort((short) (Flags.SYN_FLAG | Flags.ACK_FLAG));
        }

        return result;
    }

    public static byte[] buildFIN(  boolean isAck ) {
        byte[] result = new byte[FLAGS_SIZE];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        if ( !isAck ) {
            buffer.putShort(Flags.FIN_FLAG);
        }
        else {
            buffer.putShort((short) (Flags.FIN_FLAG | Flags.ACK_FLAG));
        }

        return result;
    }

    public static byte[] buildRST( ) {
        byte[] result = new byte[FLAGS_SIZE];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer.putShort(Flags.RST_FLAG);

        return result;
    }

    public static byte[] buildDEFAULT( int sequenceNumber, byte[] data ) {

        byte[] result = new byte[FLAGS_SIZE + SEQUENCE_NUMBER_SIZE + ACK_NUMBER_SIZE + MT + SIZE_OF_DATA + data.length];

        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer.putShort(Flags.DEF_FLAG);
        buffer.putInt(sequenceNumber);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(data.length);
        buffer.put(data, 0, data.length);
        return result;
    }

    public static short getFlag ( byte[] data ) {
        return ByteBuffer.wrap(data).getShort();
    }

    public static int getSeq ( byte[] data ) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(FLAGS_SIZE);
        return buffer.getInt();
    }

    public static int getAck( byte[] data ) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(FLAGS_SIZE + SEQUENCE_NUMBER_SIZE);
        return buffer.getInt();
    }

    public static int getMt( byte[] data ) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(FLAGS_SIZE + SEQUENCE_NUMBER_SIZE + ACK_NUMBER_SIZE);
        return buffer.getInt();
    }

    public static int getDataSize( byte[] data ) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(FLAGS_SIZE + SEQUENCE_NUMBER_SIZE + ACK_NUMBER_SIZE + MT);
        return buffer.getInt();
    }

    public static byte[] getData( byte[] data ) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            int dataSize = getDataSize(data);
            byte[] returnBytes = new byte[dataSize];
            buffer.position(FLAGS_SIZE + SEQUENCE_NUMBER_SIZE + ACK_NUMBER_SIZE + MT + SIZE_OF_DATA);
            buffer.get(returnBytes);
        return returnBytes;
    }

    public static short getHeaderSize() {
        return FLAGS_SIZE + ACK_NUMBER_SIZE + MT + SIZE_OF_DATA;
    }


    private static final short FLAGS_SIZE = 2;
    private static final short SEQUENCE_NUMBER_SIZE = 4;
    private static final short ACK_NUMBER_SIZE = 4;
    private static final short MT = 4;
    private static final short SIZE_OF_DATA = 4;
}

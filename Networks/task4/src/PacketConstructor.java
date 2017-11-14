/*

    PacketConstructor builds packet
    Each packet consists:

    -------------------------------
    |  FLAG   |   SEQ   |   ACK   |
    |-----------------------------|
    |   MT    |   HASH  |  DATA   |
    -------------------------------

    FLAG -- Points out what kind of packet it is.
            -- SYN (for connection state)
            -- ACK (used for any ack) ACK field is used
            -- RST (in case something is wrong)
            -- FIN (for closing connection)

    SEQ  -- Sequence number.

    ACK  -- How many bytes received ( in case ACK is set ).

    MT   -- The size of receivers BUFFER. ( how many bytes server is ready to receive )

    DATA -- Target data.

*/

import java.nio.ByteBuffer;
import java.util.Arrays;

public class PacketConstructor {

    public static byte[] buildACK( int sequenceNumber , int bytesReceived, int bufferSpaceAvailable ) {
        byte[] result = new byte[FLAGS_SIZE + SEQUENCE_NUMBER_SIZE + ACK_NUMBER_SIZE + MT + DATA_SIZE];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer.putShort(Flags.ACK_FLAG);
        buffer.putInt(sequenceNumber);
        buffer.putInt(bytesReceived);
        buffer.putInt(bufferSpaceAvailable);
        int hashCode = Arrays.hashCode(result);
        buffer.putInt(hashCode);
        return result;
    }

    public static byte[] buildSYQ( int sequenceNumber, int bufferSpaceAvailable ) {
        byte[] result = new byte[FLAGS_SIZE + SEQUENCE_NUMBER_SIZE + ACK_NUMBER_SIZE + MT + DATA_SIZE + HASH_CODE_SIZE];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer.putShort(Flags.SYQ_FLAG);
        buffer.putInt(sequenceNumber);
        buffer.putInt(0);
        buffer.putInt(bufferSpaceAvailable);
        int hashCode = Arrays.hashCode(result);
        buffer.putInt(hashCode);
        return result;
    }

    public static byte[] buildFIN( int sequenceNumber ) {
        byte[] result = new byte[FLAGS_SIZE + SEQUENCE_NUMBER_SIZE + ACK_NUMBER_SIZE + MT + DATA_SIZE + HASH_CODE_SIZE];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer.putShort(Flags.FIN_FLAG);
        buffer.putInt(sequenceNumber);
        buffer.putInt(0);
        buffer.putInt(0);
        int hashCode = Arrays.hashCode(result);
        buffer.putInt(hashCode);
        return result;
    }

    public static byte[] buildRST( int sequenceNumber ) {
        byte[] result = new byte[FLAGS_SIZE + SEQUENCE_NUMBER_SIZE + ACK_NUMBER_SIZE + MT + DATA_SIZE + HASH_CODE_SIZE];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer.putShort(Flags.RST_FLAG);
        buffer.putInt(sequenceNumber);
        buffer.putInt(0);
        buffer.putInt(0);
        int hashCode = Arrays.hashCode(result);
        buffer.putInt(hashCode);
        return result;
    }

    public static byte[] buildDEFAULT( int sequenceNumber, byte[] data ) {
        if ( data.length > DATA_SIZE ) {
            System.out.println("Too many data");
            return null;
        }

        byte[] result = new byte[FLAGS_SIZE + SEQUENCE_NUMBER_SIZE + ACK_NUMBER_SIZE + MT + DATA_SIZE + HASH_CODE_SIZE];

        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer.putShort(Flags.DEF_FLAG);
        buffer.putInt(sequenceNumber);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.put(data, 0, data.length);
        buffer.position(FLAGS_SIZE + SEQUENCE_NUMBER_SIZE + ACK_NUMBER_SIZE + MT);
        int hashCode = Arrays.hashCode(result);
        buffer.putInt(hashCode);
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

    public static int getAuthenticHash( byte[] data ) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(FLAGS_SIZE + SEQUENCE_NUMBER_SIZE + ACK_NUMBER_SIZE + MT);
        return buffer.getInt();
    }

    public static byte[] getData( byte[] data ) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(FLAGS_SIZE + SEQUENCE_NUMBER_SIZE + ACK_NUMBER_SIZE + MT + HASH_CODE_SIZE);
        byte[] returnBytes = new byte[data.length - FLAGS_SIZE - ACK_NUMBER_SIZE - SEQUENCE_NUMBER_SIZE - MT - HASH_CODE_SIZE];
        buffer.get(returnBytes);
        return returnBytes;
    }

    public static short getPacketSize() {
        return FLAGS_SIZE + SEQUENCE_NUMBER_SIZE + ACK_NUMBER_SIZE + HASH_CODE_SIZE + MT + DATA_SIZE;
    }


    private static final short FLAGS_SIZE = 1;
    private static final short SEQUENCE_NUMBER_SIZE = 4;
    private static final short ACK_NUMBER_SIZE = 4;
    private static final short HASH_CODE_SIZE = 4;
    private static final short MT = 4;
    private static final short DATA_SIZE = 64;
}

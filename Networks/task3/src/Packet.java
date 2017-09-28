/*
    Set of functions that work with bytes arrays send and received in packages
 */

//!!!difficult

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

public class Packet {
    public static UUID getUserID( byte[] inputBytes ) {
        ByteBuffer bb  = ByteBuffer.wrap(inputBytes);
        long mostSign = bb.getLong();
        long leastSign = bb.getLong();
        return new UUID(leastSign, mostSign);
    }

    public static short getPacketType( byte[] inputBytes ){
        ByteBuffer bb =  ByteBuffer.wrap(inputBytes);
        bb.position(GUID_SIZE);
        return bb.getShort();
    }

    public static int getNickLength( byte[] inputBytes ) {
        ByteBuffer bb = ByteBuffer.wrap(inputBytes);
        bb.position(GUID_SIZE  + TYPE_SIZE);
        return bb.getInt();
    }

    public static String getNickName( byte[] inputBytes ) {
        ByteBuffer bb = ByteBuffer.wrap(inputBytes);
        bb.position(GUID_SIZE + TYPE_SIZE + NICK_LEN_SIZE);
        return Charset.defaultCharset().decode(bb).toString();
    }

    public static String getText( byte[] inputBytes ) {
        ByteBuffer bb = ByteBuffer.wrap(inputBytes);
        bb.position(GUID_SIZE + TYPE_SIZE);
        int length = bb.getInt();
        bb.position(GUID_SIZE + TYPE_SIZE + NICK_LEN_SIZE + length);
        return Charset.defaultCharset().decode(bb).toString();
    }

    public static SocketAddress getSocketAddress( byte[] inputBytes ) throws UnknownHostException   {
        ByteBuffer bb = ByteBuffer.wrap(inputBytes);
        byte[] result = new byte[ADDRESS_SIZE];
        bb.get(result, GUID_SIZE + TYPE_SIZE, ADDRESS_SIZE);
        bb.position(GUID_SIZE + TYPE_SIZE + ADDRESS_SIZE);

        return new InetSocketAddress( InetAddress.getByAddress(result), bb.getInt());
    }

    //constructs any other type of packet handled by protocol
    public static byte[] constructPacket(UUID id, short type) {
        byte[] result = new byte[GUID_SIZE + TYPE_SIZE];
        ByteBuffer bb = ByteBuffer.wrap(result);
        bb.putLong(id.getLeastSignificantBits());
        bb.putLong(id.getMostSignificantBits());
        bb.putShort(type);
        bb.get(result);
        return result;
    }

    //constructs text packet
    public static byte[] constructPacket(User user, String text) {
        byte[] result = new byte[GUID_SIZE + TYPE_SIZE + NICK_LEN_SIZE + text.length()];
        ByteBuffer bb = ByteBuffer.wrap(result);
        UUID id = user.getID();
        short type = PacketType.TEXT;
        int nickLength = user.getNickname().length();
        String nickName = user.getNickname();
        bb.putLong(id.getMostSignificantBits());
        bb.putLong(id.getLeastSignificantBits());
        bb.putShort(type);
        bb.putInt(nickLength);
        bb.put(Charset.defaultCharset().encode(nickName));
        bb.put(Charset.defaultCharset().encode(text));
        bb.get(result);
        return result;
    }

    //constructs new parent request
    public static byte[] constructPacket(User inputUser) {
        byte[] result = new byte[GUID_SIZE + TYPE_SIZE + ADDRESS_SIZE];
        ByteBuffer bb = ByteBuffer.wrap(result);
        UUID id = inputUser.getID();
        InetAddress inputAddr = inputUser.getAddress();
        bb.putLong(id.getMostSignificantBits());
        bb.putLong(id.getLeastSignificantBits());
        bb.putShort(PacketType.PARENT);
        bb.put(inputAddr.getAddress());
        bb.get(result);
        return result;
    }


    private final static int GUID_SIZE = 16;
    private final static int TYPE_SIZE = 2;
    private final static int NICK_LEN_SIZE = 4;
    private final static int ADDRESS_SIZE = 4; // for IPv4

}

class PacketException extends Exception {

}

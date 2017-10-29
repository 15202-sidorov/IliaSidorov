/*
    Set of functions that work with bytes arrays send and received in packages
 */

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

public class PacketHandler {
    public static UUID getUserID( byte[] inputBytes ) {
        ByteBuffer bb  = ByteBuffer.wrap(inputBytes);
        long leastSign = bb.getLong();
        long mostSign = bb.getLong();
        return new UUID(mostSign, leastSign);
    }

    public static short getPacketType( byte[] inputBytes ){
        ByteBuffer bb =  ByteBuffer.wrap(inputBytes);
        bb.position(GUID_SIZE);
        return bb.getShort();
    }

    public static int getNickLength( byte[] inputBytes ) {
        if ( PacketType.TEXT != getPacketType(inputBytes) ) {
            throw new PacketTypeException("No nickname in this package");
        }

        ByteBuffer bb = ByteBuffer.wrap(inputBytes);
        bb.position(GUID_SIZE  + TYPE_SIZE);
        return bb.getInt();
    }

    public static String getNickName( byte[] inputBytes ) {
        if ( PacketType.TEXT != getPacketType(inputBytes) ) {
            throw new PacketTypeException("No nickname in this package");
        }

        ByteBuffer bb = ByteBuffer.wrap(inputBytes);
        bb.position(GUID_SIZE + TYPE_SIZE + NICK_LEN_SIZE);
        return Charset.defaultCharset().decode(bb).subSequence(0,getNickLength(inputBytes)).toString();
    }

    public static String getText( byte[] inputBytes ) {
        if ( PacketType.TEXT != getPacketType(inputBytes) ) {
            throw new PacketTypeException("No text in this package");
        }

        ByteBuffer bb = ByteBuffer.wrap(inputBytes);
        bb.position(GUID_SIZE + TYPE_SIZE);
        int length = bb.getInt();
        bb.position(GUID_SIZE + TYPE_SIZE + NICK_LEN_SIZE + length);
        return Charset.defaultCharset().decode(bb).toString();
    }

    public static InetSocketAddress getSocketAddress( byte[] inputBytes ) throws UnknownHostException   {
        if ( PacketType.PARENT != getPacketType(inputBytes) ) {
           throw new PacketTypeException("No socket address in this package");
        }

        ByteBuffer bb = ByteBuffer.wrap(inputBytes);
        byte[] resultAddr = new byte[ADDRESS_SIZE];
        int resultPort;
        bb.position(GUID_SIZE + TYPE_SIZE);
        bb.get(resultAddr, 0, ADDRESS_SIZE);
        resultPort = bb.getInt();

        return new InetSocketAddress(InetAddress.getByAddress(resultAddr), resultPort);
    }

    //constructs any other type of packet handled by protocol
    public static byte[] constructPacket(UUID id, short type) {
        if ((type == PacketType.PARENT) ||
           (type == PacketType.ROOT) ||
           (type == PacketType.TEXT)) {
            throw new PacketTypeException("Bad constructor for such type");
        }
        byte[] result = new byte[GUID_SIZE + TYPE_SIZE];
        ByteBuffer bb = ByteBuffer.wrap(result);
        bb.putLong(id.getLeastSignificantBits());
        bb.putLong(id.getMostSignificantBits());
        bb.putShort(type);
        return result;
    }

    //constructs text packet
    public static byte[] constructTextPacket(User user, String text) {
        UUID id = user.getID();
        short type = PacketType.TEXT;
        int nickLength = user.getNickname().length();
        String nickName = user.getNickname();
        byte[] result = new byte[GUID_SIZE + TYPE_SIZE + NICK_LEN_SIZE + nickName.length() + text.length()];
        ByteBuffer bb = ByteBuffer.wrap(result);
        bb.putLong(id.getLeastSignificantBits());
        bb.putLong(id.getMostSignificantBits());
        bb.putShort(type);
        bb.putInt(nickLength);
        bb.put(Charset.defaultCharset().encode(nickName));
        bb.put(Charset.defaultCharset().encode(text));
        return result;
    }

    //constructs new parent request
    public static byte[] constructParentPacket(UUID ownerID, InetSocketAddress parentUserAddr) {
        byte[] result = new byte[GUID_SIZE + TYPE_SIZE + ADDRESS_SIZE + PORT_SIZE];
        ByteBuffer bb = ByteBuffer.wrap(result);
        bb.putLong(ownerID.getLeastSignificantBits());
        bb.putLong(ownerID.getMostSignificantBits());
        bb.putShort(PacketType.PARENT);
        bb.put(parentUserAddr.getAddress().getAddress());
        bb.putInt(parentUserAddr.getPort());
        return result;
    }

    private final static short GUID_SIZE = 16;
    private final static short TYPE_SIZE = 2;
    private final static short NICK_LEN_SIZE = 4;
    private final static short ADDRESS_SIZE = 4; // for IPv4
    private final static short PORT_SIZE = 4;
}

class PacketTypeException extends NullPointerException {
    PacketTypeException(String message) {
        super(message);
    }
}



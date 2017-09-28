/*
    All types of messages allowed by protocol
 */

public class PacketType {
    public static final short CONNECT = 0;
    public static final short DISCONNECT = 1;
    public static final short TEXT = 2;
    public static final short ACK = 3;
    public static final short PARENT = 4;
    public static final short ROOT = 5;

    //checks out whether protocol excepts that kind of command
    public static boolean exists(short input) {
        return ((input >= 0) && (input <= 5));
    }
}

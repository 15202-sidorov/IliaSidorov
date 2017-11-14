public class Flags {
    public static final short ACK_FLAG = Short.parseShort("00000001",2);
    public static final short SYQ_FLAG = Short.parseShort("00000010", 2);
    public static final short RST_FLAG = Short.parseShort("00000100", 2);
    public static final short FIN_FLAG = Short.parseShort("00001000",2);
    public static final short DEF_FLAG = Short.parseShort("00010000", 2);
}

/*
    Class describes some properties of user
 */


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class User {
    public User( String inputNickname, InetSocketAddress inputAddr ) throws UnknownHostException {
        Nickname = inputNickname;
        address = inputAddr;
    }

    public User( InetSocketAddress inputAddr ) {
        address = inputAddr;
    }

    public UUID getID() {
        return id;
    }

    public String getNickname() {
        return Nickname;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    private String Nickname = DEFAULT_NICKNAME;
    private UUID id = UUID.randomUUID();
    private InetSocketAddress address;

    private final static String DEFAULT_NICKNAME = "UnknownName";
}

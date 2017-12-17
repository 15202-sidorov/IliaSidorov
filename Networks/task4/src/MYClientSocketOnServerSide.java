
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class MYClientSocketOnServerSide extends MYClientSocket {
    public MYClientSocketOnServerSide(DatagramSocket socket,
                                      ConnectionStatus status) {
        super(socket,status);

    }
}



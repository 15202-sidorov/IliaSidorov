import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class MessageSender {
    MessageSender(SocketChannel s1) throws IOException {
        clientSocket = s1;
        clientSocket.configureBlocking(true);
        messageBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    }

    void sendMessage(String message) throws IOException {
        messageBuffer.clear();
        messageBuffer.put(MAIN_CHARSET.encode(message));
        messageBuffer.flip();
        clientSocket.write(messageBuffer);
    }

    String receiveMessage() throws IOException {
        messageBuffer.clear();
        clientSocket.read(messageBuffer);
        messageBuffer.flip();
        return MAIN_CHARSET.decode(messageBuffer).toString();
    }

    void sendBuffer( ByteBuffer buffer ) throws IOException {
        buffer.flip();
        clientSocket.write(buffer);
        buffer.flip();
        buffer.clear();
    }

    void receiveBuffer( ByteBuffer buffer ) throws IOException {
        buffer.clear();
        clientSocket.read(buffer);
        buffer.flip();
    }

    private SocketChannel clientSocket;
    private ByteBuffer messageBuffer;

    private static final int BUFFER_CAPACITY = 64;
    private static final Charset MAIN_CHARSET = Charset.forName("UTF-8");
}
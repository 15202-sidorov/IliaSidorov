import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;


//bytebuffer!!!! be careful
//send buffer,size ->
//check out the name and parse it /../../../text.exe
//check out speed and average speed every three seconds
class SendingThread extends Thread {
    SendingThread( String[] args ) {
        filePath = args[0];
        try {
            serverAddress = new InetSocketAddress(InetAddress.getByName(args[1]), Integer.parseInt(args[2]));
        }
        catch( UnknownHostException ex ) {
            System.out.println("Could not find server");
        }
    }

    @Override
    public void run() {
        try {
            channel = SocketChannel.open(serverAddress);
            buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
            sendSuccess();
            System.out.println("Message is send, closing connection");

        }
        catch ( IOException ex ) {
            System.out.println("IOException in run");
        }
        finally {
            try {
                channel.close();
                System.out.println("Channel is closed successfully");
            }
            catch ( IOException ex ) {
                System.out.println("Could not close connection correctly");
            }

        }
    }

    private void sendFailure() throws IOException {
        buffer.clear();
        buffer.put(MAIN_CHARSET.encode(FAIL_MESSAGE));
        channel.write(buffer);
        buffer.clear();
    }

    private void sendSuccess() throws IOException {
        buffer.clear();
        buffer.put(MAIN_CHARSET.encode(SUCCESS_MESSAGE));
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }

    private String filePath;
    private InetSocketAddress serverAddress;
    private int fileSize;
    private SocketChannel channel;
    private ByteBuffer buffer;

    private static final int BUFFER_CAPACITY = 64;
    private static final Charset MAIN_CHARSET = Charset.forName("UTF-8");
    private static final String SUCCESS_MESSAGE = "SUCCESS";
    private static final String FAIL_MESSAGE = "FAILURE";

}

public class Client {
    public static void main( String[] args ) {
        SendingThread thread = new SendingThread(args);
        thread.start();

        try {
            thread.join();
        }
        catch ( InterruptedException ex ){
            thread.interrupt();
            Thread.currentThread().interrupt();
        }
    }
}

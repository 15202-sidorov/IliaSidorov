import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
        buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
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
            connectionRequest();
            if ( !sendFileName() ) {
                return;
            }

            if ( !sendFileSize() ) {
                return;
            }

            sendFile();
        }
        catch ( IOException ex ) {
            System.out.println("IOException in run");
        }
        finally {
            try {
                outputStream.close();
                channel.close();
                System.out.println("Channel is closed successfully");
            }
            catch ( IOException ex ) {
                System.out.println("Could not close connection correctly");
            }

        }
    }


    private void connectionRequest() throws IOException {
        System.out.println("Sending connection request...");
        channel = SocketChannel.open(serverAddress);
        outputStream = new DataOutputStream(channel.socket().getOutputStream());
        sender = new MessageSender(channel);
        sender.sendMessage(Message.CONNECT);
        if ( sender.receiveMessage().equals(Message.FAIL) ) {
            System.out.println("Connection is not established");
            return;
        }
        System.out.println("Connection is established");
    }

    private boolean sendFileName() throws IOException {
        System.out.println("Sending file path...");
        sender.sendMessage(filePath);

        if ( sender.receiveMessage().equals(Message.FAIL) ) {
            System.out.println("Could not send file path");
            return false;
        }
        System.out.println("File path is send successfully");
        return true;

    }

    private boolean sendFileSize() throws IOException {
        System.out.println("Sending file size...");
        outputFile = new File(filePath);
        fileSize = outputFile.length();
        outputStream.writeLong(fileSize);
        if ( sender.receiveMessage().equals(Message.FAIL) ) {
            System.out.println("Server doesn't accept file size");
            return false;
        }
        System.out.println("File size is send successfully");
        return true;
    }

    private boolean sendFile() throws IOException {
        System.out.println("Sending file...");
        FileInputStream inputStream = new FileInputStream(outputFile);
        int bytesSend = 0;
        while ( bytesSend < fileSize ) {

            byte[] data = new byte[BUFFER_CAPACITY];
            if (-1 == inputStream.read(data)) {
                System.out.println("Could not read bytes");
                break;
            }

            buffer.put(data);
            sender.sendBuffer(buffer);
            bytesSend += BUFFER_CAPACITY;
            System.out.println(bytesSend);
        }

        if ( sender.receiveMessage().equals(Message.SUCCESS) ) {
            System.out.println("File is send successfully");
            return true;
        }
        else {
            System.out.println("Could not send file");
            return false;
        }
    }

    private long fileSize = 0;
    private File outputFile = null;
    private String filePath = null;
    private InetSocketAddress serverAddress = null;
    private SocketChannel channel = null;
    private DataOutputStream outputStream = null;
    private ByteBuffer buffer = null;
    private MessageSender sender = null;

    private static final int BUFFER_CAPACITY = 64;
    private static final Charset MAIN_CHARSET = Charset.forName("UTF-8");


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

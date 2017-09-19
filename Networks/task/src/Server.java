import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

class ClientHandler extends Thread {
    ClientHandler( SocketChannel channel ) throws IOException {
        clientSocket = channel;
        mesSender = new MessageSender(channel);
        buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    }

    ClientHandler ( SocketChannel channel, MessageSender mes ) {
        clientSocket = channel;
        mesSender = mes;
        buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    }

    public void run() {
        try {
            if ( !receiveFileName() ) {
                clientSocket.close();
                return;
            }

            if ( !receiveFileSize() ) {
                clientSocket.close();
                return;
            }

            receiveFile();
        }
        catch ( IOException ex ) {
            System.out.println();
        }
        finally {
            try {
                clientSocket.close();
            }
            catch ( IOException ex ) {
                System.out.println("Could not close client socket");
            }
        }

    }

    private boolean receiveFileName() throws IOException {
        mesSender.receiveBuffer(buffer);
        fileName = Charset.forName("UTF-8").decode(buffer).toString();
        if ( checkFileName(fileName) ) {
            mesSender.sendMessage(Message.SUCCESS);
            return true;
        }
        else {
            mesSender.sendMessage(Message.FAIL);
            return false;
        }
    }

    private boolean receiveFileSize() throws IOException {
        mesSender.receiveBuffer(buffer);
        fileSize = buffer.getInt();
        if (fileSize > MAX_FILE_SIZE) {
            mesSender.sendMessage(Message.FAIL);
            return false;
        }
        else {
            mesSender.sendMessage(Message.SUCCESS);
            return true;
        }
    }

    private void receiveFile() throws IOException {
        File receivedFile = new File(FILE_PATH + fileName);
        FileOutputStream outputStream = new FileOutputStream(receivedFile);
        while (bytesRead < fileSize) {
            mesSender.receiveBuffer(buffer);
            outputStream.write(buffer.get());
            bytesRead += BUFFER_CAPACITY;
        }

        mesSender.sendMessage(Message.SUCCESS);
    }

    private boolean checkFileName( String name ) {
        return true;
    }

    private SocketChannel clientSocket = null;
    private int bytesRead = 0;
    private ByteBuffer buffer = null;
    private MessageSender mesSender = null;
    private String fileName = null;
    private int fileSize = 0;
    private static final String FILE_PATH = "D:\\IliaSidorov\\Networks\\src\\uploads";
    private static final int MAX_FILE_SIZE = 1024;
    private static final int BUFFER_CAPACITY = 64;
}

class ReceiveThread extends Thread {
    ReceiveThread( int input_port ) {
        createDirectory();
        PORT_NUMBER = input_port;
    }

    @Override
    public void run() {
        try {
            initServerSocket();
            //createDirectory();
            System.out.println("Server is ready to receive messages");
            System.out.println("Address : " + channel.socket().getInetAddress() );
            System.out.println("Port : " + PORT_NUMBER);

            String message = null;
            //while ( !this.isInterrupted() ) {
                System.out.println("Waiting for client...");
                SocketChannel receivedChannel = channel.accept();
                MessageSender mesSender = new MessageSender(receivedChannel);
                message = mesSender.receiveMessage();
                /*if ( message.equals(Message.CONNECT) ) {
                    mesSender.sendMessage(Message.SUCCESS);
                    new ClientHandler( receivedChannel, mesSender ).start();
                }*/
                System.out.println(message);
                System.out.println("New client is connected");
            //}

        }
        catch ( IOException ex ) {
            System.out.println("Exception caught");
            System.out.println("Main thread terminating");
        }
        finally {
            try {
                channel.close();
            }
            catch ( IOException ex1 ) {
                System.out.println("Could not close Server socket");
            }
        }

    }

    private void initServerSocket() throws IOException {
        InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(),PORT_NUMBER);
        channel = ServerSocketChannel.open();
        channel.bind(address);
    }

    private void createDirectory() {
        File directory = new File(DIRECTORY_PATH);
        if ( !directory.exists() ) {
            if ( directory.mkdir() ) {
                System.out.println("Directory is created");
            }
            else {
                System.out.println("Could not create directory");
            }
        }
        else {
            System.out.println("Directory already exists");
        }
    }

    private ServerSocketChannel channel;
    private int PORT_NUMBER;

    private static final String DIRECTORY_PATH = "D:\\IliaSidorov\\Networks\\src\\uploads";

}


public class Server {
    static public void main (String[] args) {
        ReceiveThread thread = new ReceiveThread(Integer.parseInt(args[0]));
        thread.start();

       /* try {
            thread.join();
        }
        catch ( InterruptedException ex ) {
            thread.interrupt();
            Thread.currentThread().interrupt();
        }*/
    }

}


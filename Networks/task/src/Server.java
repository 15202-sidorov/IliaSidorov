import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

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

    ClientHandler ( SocketChannel channel, MessageSender mes ) throws IOException {
        clientSocket = channel;
        inputStream = new DataInputStream(clientSocket.socket().getInputStream());
        mesSender = mes;
        buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    }

    public void run() {
        try {
            if ( !receiveFileName() ) {
                inputStream.close();
                clientSocket.close();
                System.out.println("Could not receive file name");

                return;
            }

            if ( !receiveFileSize() ) {
                inputStream.close();
                clientSocket.close();
                System.out.println("Could not receive file size");
                return;
            }

            receiveFile();
        }
        catch ( IOException ex ) {
            System.out.println("IOException caught in handler");
            try {
                mesSender.sendMessage(Message.FAIL);
            }
            catch ( IOException ex1 ) {
                System.out.println("Could not send fail message");
            }
        }
        finally {
            try {
                outputStream.close();
                inputStream.close();
                clientSocket.close();
                System.out.println("Everything is all right, terminating successfully");
            }
            catch ( IOException ex ) {
                System.out.println("Could not close client socket");
            }
        }
    }

    private boolean receiveFileName() throws IOException {
        System.out.println("Receiving file name...");
        buffer = mesSender.receiveBuffer();
        buffer.rewind();
        fileName = Charset.forName("UTF-8").decode(buffer).toString();
        buffer.clear();
        System.out.println("File name is : " + fileName);
        if ( checkFileName(fileName) ) {
            System.out.println("File name is receeived successfully");
            System.out.println("Sending confirmation request");
            mesSender.sendMessage(Message.SUCCESS);
            return true;
        }
        else {
            System.out.println("File name is not correct");
            System.out.println("Sending fail");
            mesSender.sendMessage(Message.FAIL);
            return false;
        }
    }

    private boolean receiveFileSize() throws IOException {
        System.out.println("Receiving file size...");
        fileSize = inputStream.readLong();
        System.out.println("File size is " + fileSize);
        if (fileSize > MAX_FILE_SIZE) {
            System.out.println("Sending fail request");
            mesSender.sendMessage(Message.FAIL);
            return false;
        }
        else {
            System.out.println("File size is all right");
            mesSender.sendMessage(Message.SUCCESS);
            return true;
        }
    }

    private void receiveFile() throws IOException {
        System.out.println("Receiving file...");
        fileName = "SomeFileYo.txt";
        System.out.println(FILE_PATH + "\\" + fileName);
        File receivedFile = new File(FILE_PATH + "\\" + fileName);
        if ( !receivedFile.createNewFile() ) {
            System.out.println("File already exists, rewriting...");
        }

        System.out.println("File is created ");
        outputStream = new FileOutputStream(receivedFile);
        System.out.println("Reading file ...");
        byte[] data = new byte[BUFFER_CAPACITY];
        while (bytesRead < fileSize) {
            buffer = mesSender.receiveBuffer();
            buffer.rewind();
            buffer.get(data);
            outputStream.write(data);
            buffer.clear();
            bytesRead += BUFFER_CAPACITY;
        }
        outputStream.close();
        mesSender.sendMessage(Message.SUCCESS);
        System.out.println("File received successfully");
    }

    private boolean checkFileName( String name ) {
        return true;
    }

    private SocketChannel clientSocket = null;
    private DataInputStream inputStream = null;
    private FileOutputStream outputStream = null;
    private int bytesRead = 0;
    private ByteBuffer buffer = null;
    private MessageSender mesSender = null;
    private String fileName = null;
    private long fileSize = 0;
    private static final String FILE_PATH = "D:\\IliaSidorov\\Networks\\task\\src\\uploads";
    private static final long MAX_FILE_SIZE = 16000;
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
            createDirectory();
            System.out.println("Server is ready to receive messages");
            System.out.println("Address : " + channel.socket().getInetAddress() );
            System.out.println("Port : " + PORT_NUMBER);

            String message = null;
            while ( !this.isInterrupted() ) {
                System.out.println("Waiting for client...");
                SocketChannel receivedChannel = channel.accept();
                MessageSender mesSender = new MessageSender(receivedChannel);
                message = mesSender.receiveMessage();
                if ( message.equals(Message.CONNECT) ) {
                    mesSender.sendMessage(Message.SUCCESS);
                    new ClientHandler( receivedChannel, mesSender ).start();
                }
                System.out.println(message);
                System.out.println("New client is connected");
            }

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

    private static final String DIRECTORY_PATH = "D:\\IliaSidorov\\Networks\\task\\src\\uploads";

}

public class Server {
    static public void main (String[] args) {
        ReceiveThread thread = new ReceiveThread(Integer.parseInt(args[0]));
        thread.start();

       try {
            thread.join();
        }
        catch ( InterruptedException ex ) {
            thread.interrupt();
            Thread.currentThread().interrupt();
        }
    }

}


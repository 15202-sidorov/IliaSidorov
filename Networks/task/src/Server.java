import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

class ReceiveThread extends Thread {
    ReceiveThread( int input_port ) {
        PORT_NUMBER = input_port;
    }

    @Override
    public void run() {
        try {
            connectToClient();
            buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
            /*createDirectory();
            receiveFileName();
            receiveFileSize();
            receiveFile();*/
            System.out.println("Server is ready to receive messages");
            System.out.println("Address : " + channel.socket().getInetAddress() );
            System.out.println("Port : " + PORT_NUMBER);

            receivedChannel = channel.accept();
            receivedChannel.read(buffer);
            buffer.flip();
            System.out.println(MAIN_CHARSET.decode(buffer).toString());
            System.out.println("Successful receive, closing connection");
        }
        catch ( IOException ex ) {
            System.out.println("Exception caught");
            System.out.println("Sending failure");
            try {
                sendFailure();
            }
            catch (IOException ex1) {
                System.out.println("Could not send final failure message");
            }
        }
        finally {
            try {
                channel.close();
                receivedChannel.close();
            }
            catch ( IOException ex1 ) {

            }
        }

    }

    private void connectToClient() throws IOException {
        InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(),PORT_NUMBER);
        channel = ServerSocketChannel.open();
        channel.bind(address);

    }

    private void createDirectory() {
        File directory = new File(FILE_PATH);
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

    private void receiveFileName() throws IOException {
        buffer.clear();
        receivedChannel.read(buffer);
        buffer.flip();
        fileName = MAIN_CHARSET.decode(buffer).toString();
        System.out.println("File name received : " + fileName );
        if ( !checkFileName(fileName) ) {
            System.out.println("Invalid filename");
            sendFailure();
        }
        else {
            System.out.println("File name is correct sending approve message");
            sendSuccess();
        }
    }

    private boolean checkFileName( String fileName ) {
        return true;
    }

    private void receiveFileSize() throws IOException {
        buffer.clear();
        receivedChannel.read(buffer);
        buffer.flip();
        fileSize = buffer.getInt();
        if ( !checkFileSize(fileSize) ) {
            System.out.println("File size exeeds limit");
            sendFailure();
        }
        else {
            System.out.println("File size is all right");
            mainFile = new File(FILE_PATH + "\\" + fileName);
            System.out.println("File is created successfully");
            sendSuccess();
        }
    }

    private void receiveFile() throws IOException {
        buffer.clear();

        FileOutputStream outputStream = new FileOutputStream(mainFile);
        Timer speedTimer = new Timer();
        speedTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                counter++;
                current_speed = (bytesRead - bytesReadBefore)/SPEED_CHECKOUT_TIME;
                bytesReadBefore = bytesRead;
                average_speed += ((average_speed * (counter - 1)) + current_speed)/counter;
                System.out.println("Current speed : " + current_speed);
                System.out.println("Average speed : " + average_speed);
            }

            private int counter = 0;
        },SPEED_CHECKOUT_TIME, SPEED_CHECKOUT_TIME);

        while ( bytesRead < fileSize ) {
            receivedChannel.read(buffer);
            outputStream.write(buffer.array());
            bytesRead += buffer.capacity();
            buffer.clear();
        }

        sendSuccess();
    }

    private void sendFailure() throws IOException {
        buffer.clear();
        buffer.put(MAIN_CHARSET.encode(FAIL_MESSAGE));
        receivedChannel.write(buffer);
        buffer.clear();
    }

    private void sendSuccess() throws IOException {
        buffer.clear();
        buffer.put(MAIN_CHARSET.encode(SUCCESS_MESSAGE));
        receivedChannel.write(buffer);
        buffer.clear();
    }

    private boolean checkFileSize( int fileSize ) {
        return (MAX_FILE_SIZE > fileSize);
    }

    private File mainFile;
    private String fileName;
    private int fileSize;
    private ServerSocket mainSocket;
    private ServerSocketChannel channel;
    private SocketChannel receivedChannel;
    private ByteBuffer buffer;
    private int PORT_NUMBER;
    private int bytesRead = 0;
    private int bytesReadBefore = 0;

    private double current_speed = 0;

    private static double average_speed = 0;

    private static final int SPEED_CHECKOUT_TIME = 3000;
    private static final Charset MAIN_CHARSET = Charset.forName("UTF-8");
    private static final int BUFFER_CAPACITY = 64;
    private static final int MAX_FILE_SIZE = 1024;
    private static final String SUCCESS_MESSAGE = "SUCCESS";
    private static final String FAIL_MESSAGE = "FAILURE";
    private static final String FILE_PATH = "D:\\IliaSidorov\\Networks\\src\\uploads";
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


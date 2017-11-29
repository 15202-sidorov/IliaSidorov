import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.UUID;

public class Server {
    public static void main( String[] args ) {
        try {
            int port = Integer.parseInt(args[0]);
            int hash = Integer.parseInt(args[1]);
            ServerSocket socket = new ServerSocket(port);
            String header = null;
            String body = null;
            ServerSocketChannel channel = socket.getChannel();
            currentStartPoint = 0;

            String currentString = "A";
            while (currentStartPoint < MAX_LENGTH) {
                Socket clientSocket = socket.accept();
                Thread clientHandler = new HandleClientThread(clientSocket, hash, currentString,  LENGTH_PER_THREAD);
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                header = reader.readLine();
                body = reader.readLine();
                if ( header.equals(Protocol.ESTABLISH_CONNECTION) ) {
                    clients.put(UUID.fromString(body), clientSocket);
                    header = Protocol.STRING_TO_CHECK;
                    body = currentString + '\n' + LENGTH_PER_THREAD;
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    writer.write(header + body);
                    clientSocket.close();
                }
                else if ( header.equals(Protocol.ANSWER_OK) ) {
                    System.out.println("Got answer from : " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                    System.out.println("Answer is : " + body);
                    clients.remove(UUID.fromString(reader.readLine()));
                    System.out.println("Terminating...");
                    continue;
                }
                else {
                    System.out.println("Not a protocol behavior while connecting");
                    continue;
                }
                clientHandler.start();

                for (int i = 0; i < LENGTH_PER_THREAD; i++) {
                    currentString += "A";
                }

            }

            while ( !clients.isEmpty() ) {
                Socket clientSocket = socket.accept();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                reader.readLine();
                clients.remove(UUID.fromString(reader.readLine()));
                header = Protocol.TERMINATE_CONNECTION;

                writer.write(header);
            }
        }
        catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    private static HashMap<UUID, Socket> clients;
    private static int currentStartPoint;

    private static final int MAX_LENGTH = 1000;
    private static final int LENGTH_PER_THREAD = 100;
}

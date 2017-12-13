import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Server {
    public static void main( String[] args ) {
        try {
            int port = Integer.parseInt(args[0]);
            String stringToCheck = "AAGGGGACT";
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(stringToCheck.getBytes());
            clients = Collections.synchronizedMap(new HashMap<UUID, Range>());
            clientsAlive = Collections.synchronizedMap(new HashMap<UUID, Boolean>());
            rangesDropped = new LinkedBlockingDeque<>();
            byte[] hash = md.digest();
            System.out.println("Hash to check : ");
            for (int i = 0; i < hash.length; i++) {
                System.out.print(hash[i]);
            }

            System.out.println();
            ServerSocket socket = new ServerSocket(port);
            String header = null;
            String body = null;

            int stringsChecked = 0;

            Timer checkAlive = new Timer();
            checkAlive.scheduleAtFixedRate(new CheckAliveClients(), 0, CHECK_ALIVE_TIME);

            while ( true ) {
                try {
                    System.out.println("Waiting for connection to be made...");
                    Socket clientSocket = socket.accept();
                    System.out.println("Connection accepted");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    header = reader.readLine();
                    body = reader.readLine();
                    System.out.println("Received header: " + header);
                    System.out.println("Received body: " + body);
                    Range range = new Range(stringsChecked, stringsChecked + LENGTH_PER_THREAD);
                    if ((header.equals(Protocol.ESTABLISH_CONNECTION)) && (stringsChecked < MAX_LENGTH)) {
                        System.out.println("Sending check string...");
                        clients.put(UUID.fromString(body), range);
                        clientsAlive.put(UUID.fromString(body), true);
                        header = Protocol.STRING_TO_CHECK + '\n';
                        body = stringsChecked + "\n" + (stringsChecked + LENGTH_PER_THREAD) + "\n" + hash.length + "\n";
                        body += Arrays.toString(hash);
                        stringsChecked += LENGTH_PER_THREAD;
                        writer.write(header + body);
                        writer.flush();
                        clientSocket.close();
                    } else if ((header.equals(Protocol.ESTABLISH_CONNECTION)) && !(stringsChecked < MAX_LENGTH)) {
                        if (rangesDropped.isEmpty()) {
                            clients.remove(UUID.fromString(body));
                            clientsAlive.remove(UUID.fromString(body));
                            header = Protocol.TERMINATE_CONNECTION;
                            writer.write(header);
                            writer.flush();
                            clientSocket.close();

                            if (clients.isEmpty()) {
                                break;
                            }
                        } else {
                            System.out.println("Dealing with dropped ranges...");
                            Range rangeDropped = rangesDropped.take();
                            System.out.println("Sending check string...");
                            clients.put(UUID.fromString(body), rangeDropped);
                            header = Protocol.STRING_TO_CHECK + '\n';
                            body = range.start + "\n" + range.end + "\n" + hash.length + "\n";
                            body += Arrays.toString(hash);
                            stringsChecked += LENGTH_PER_THREAD;
                            writer.write(header + body);
                            writer.flush();
                            clientSocket.close();
                        }
                    } else if (header.equals(Protocol.ANSWER_OK)) {
                        System.out.println("Got answer from : " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                        System.out.println("Answer is : " + body);
                        clients.remove(UUID.fromString(reader.readLine()));
                        System.out.println("Terminating...");
                        clientSocket.close();

                        if (clients.isEmpty()) {
                            break;
                        }

                    } else {
                        System.out.println("Not a protocol behavior while connecting");
                        continue;
                    }
                }
                catch ( SocketException ex ) {
                    rangesDropped.add(new Range(stringsChecked, stringsChecked + LENGTH_PER_THREAD));
                }

            }

            System.out.println("LENGTH EXCEEDED");

        }
        catch ( IOException ex ) {
            ex.printStackTrace();
        }
        catch ( NoSuchAlgorithmException ex ) {
            ex.printStackTrace();
        }
        catch ( InterruptedException ex ) {
            Thread.currentThread().interrupt();
        }
    }

    static class CheckAliveClients extends TimerTask {
        public void run() {
            for ( UUID id : clientsAlive.keySet() ) {
                if ( clientsAlive.get(id) ) {
                    clientsAlive.put(id, false);
                }
                else {
                    System.out.println("TIMEOUT for " + id);
                    Range range = clients.get(id);
                    rangesDropped.add(range);
                    clientsAlive.remove(id);
                    clients.remove(id);
                }
            }
        }
    }

    private static Map<UUID, Range> clients;
    private static Map<UUID, Boolean> clientsAlive;
    private static BlockingQueue<Range> rangesDropped;

    private static final int MAX_LENGTH = 1000000;
    private static final int LENGTH_PER_THREAD = 100;
    private static final int CHECK_ALIVE_TIME = 10000;
}

class Range {
    Range( int inputStart, int inputEnd ) {
        start = inputStart;
        end = inputEnd;
    }

    int start;
    int end;
}



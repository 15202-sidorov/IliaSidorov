import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    public static void main( String[] args ) {
        try {
            InetAddress address = InetAddress.getLocalHost();
            int port = Integer.parseInt(args[0]);
            UUID id = UUID.randomUUID();
            Socket socket = null;
            BufferedWriter writer = null;
            BufferedReader reader = null;

            while (true) {
                socket = new Socket(address,port);
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                reader =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer.write(Protocol.ESTABLISH_CONNECTION + '\n');
                writer.write(id.toString() + "\n");
                writer.flush();
                System.out.println("Request is send");

                String head = reader.readLine();
                System.out.println("Server answered : " + head);
                if ( head.equals(Protocol.STRING_TO_CHECK) ) {
                    int startString = Integer.parseInt(reader.readLine());
                    int endString = Integer.parseInt(reader.readLine());
                    int hashSize = Integer.parseInt(reader.readLine());
                    System.out.println("Server made request to check from : " + startString + " to " + endString);
                    byte[] hash = new byte [hashSize];
                    System.out.println("Hash length is : " + hashSize);
                    String stringArray = reader.readLine();
                    Pattern p = Pattern.compile("-?\\d+");
                    Matcher m = p.matcher(stringArray);
                    int byteRead = 0;
                    while (m.find()) {
                        hash[byteRead] = Byte.parseByte(m.group());
                        byteRead++;
                    }

                    System.out.println("Hash is read : ");
                    for (int i = 0; i < hash.length; i++) {
                        System.out.print(hash[i]);
                    }
                    System.out.println();

                    System.out.println("Starting to check...");
                    StringGenerator generator = new StringGenerator(startString, endString);
                    String gotResult = generator.checkHash(hash);
                    if ( null != gotResult ) {
                        socket = new Socket(address, port);
                        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        writer.write(Protocol.ANSWER_OK + "\n");
                        writer.write(gotResult + "\n");
                        writer.write(id.toString() + '\n');
                        writer.flush();
                        break;
                    }
                }
                else if ( head.equals(Protocol.TERMINATE_CONNECTION) ) {
                    System.out.println("Terminate connection received");
                    return;
                }
                else {
                    System.out.println("Not a protocol behavior");
                }
            }
        }
        catch ( Exception ex ) {
            ex.printStackTrace();
        }


    }
}

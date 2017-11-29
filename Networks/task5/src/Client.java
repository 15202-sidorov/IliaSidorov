import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.UUID;

public class Client {
    public void main( String[] args ) {
        try {
            InetAddress address = InetAddress.getLocalHost();
            int port = Integer.parseInt(args[0]);
            UUID id = UUID.randomUUID();
            Socket socket = new Socket(address, port);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.write(Protocol.ESTABLISH_CONNECTION);
            writer.write(id.toString());

            String head = reader.readLine();
            String body = reader.readLine();
            String letters = reader.readLine();

            if ( head.equals(Protocol.STRING_TO_CHECK) ) {
                System.out.println("Server made request to check string : " + body);
                System.out.println(letters);
                System.out.println("Starting to check...");
                StringBuilder builder = new StringBuilder(body);
                String currentString = null;
                for (int i = 0; i < Integer.parseInt(letters); i++) {
                    currentString = builder.getNext();
                    //check md5 hash
                }
            }
        }
        catch ( Exception ex ) {
            ex.printStackTrace();
        }


    }
}

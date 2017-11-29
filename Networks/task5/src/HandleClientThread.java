import java.io.*;
import java.net.Socket;

public class HandleClientThread extends Thread {
    public HandleClientThread( Socket inputSocket, int hashGiven, String startString, int moreLetters ) throws IOException {
        socket = inputSocket;
        currentString = startString;
        letters = moreLetters;
        hash = hashGiven;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void run() {
        try {
            String header = Protocol.STRING_TO_CHECK;
            String body = currentString + '\n' + letters;
            writer.write(header + body, 0, (header + body).length());
            socket.close();
        }
        catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String currentString;
    private int letters;
    private int hash;
}

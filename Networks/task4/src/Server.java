
public class Server {
    public static void main( String[] args ) {
        try {
            MYServerSocket socket = new MYServerSocket(8000);
            MYClientSocketOnServerSide clientSocket = socket.accept();
            clientSocket.send("Hello world00000000000000sdf;sdjk;sljd;lkcvjboierjkjxc;lkvjiojsdljkoijwklxcjv;lkj".getBytes());
            clientSocket.send("Another hello there".getBytes());
            clientSocket.close();
        }
        catch ( Exception ex ) {
            ex.printStackTrace();
        }

    }

}

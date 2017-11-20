import java.io.IOException;

public interface MYSocket {
   void send(byte[] data) throws IOException, InterruptedException;
   byte[] receive() throws IOException, InterruptedException;
   void close();
}

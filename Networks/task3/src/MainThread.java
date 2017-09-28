public class MainThread {
    public static void main( String args[] ) {

        nodeThread.start();
        try {
            nodeThread.join();
        }
        catch ( InterruptedException ex ) {
            System.out.println("Node is Interrupted");
            nodeThread.interrupt();
            Thread.currentThread().interrupt();
        }
    }
}

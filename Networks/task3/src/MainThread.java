
import java.net.*;

public class MainThread {
    public static void main( String args[] ) {
        try {

            String nickName = args[0];
            System.out.println(args.length);
            User nodeUser = new User(nickName, new InetSocketAddress(InetAddress.getLocalHost(), 0));
            Node mainNode;

            if (args[1].equals("ROOT")) {
                mainNode = new Node(nodeUser);
                mainNode.start();
            } else {
                if ( args.length != 3 ) {
                    System.out.println("no argument");
                    return;
                }
                mainNode = new Node(nodeUser, new InetSocketAddress(InetAddress.getByName(args[1]), Integer.parseInt(args[2])));

                mainNode.start();
            }


        }
        catch ( Exception ex ) {
            ex.printStackTrace();
        }


    }
}

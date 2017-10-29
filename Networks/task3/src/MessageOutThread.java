/*

    Thread prints all messages received by other threads.

 */

import java.util.concurrent.SynchronousQueue;

public class MessageOutThread extends Thread{
    public MessageOutThread(SynchronousQueue<String> inputMessageQueue) {
        messageQueue = inputMessageQueue;
    }

    @Override
    public void run() {
        try {
            while ( !Thread.currentThread().isInterrupted() ) {
                String messageTaken = messageQueue.take();
                System.out.println(messageTaken);
            }
        }
        catch ( InterruptedException ex ) {
            Thread.currentThread().interrupt();
        }
    }

    private SynchronousQueue<String> messageQueue;
}

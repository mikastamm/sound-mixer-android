package mikastamm.com.soundmixer.Networking;

import android.util.Log;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.MessageHandlers.ReceivedMessageHandler;
import mikastamm.com.soundmixer.ServerList;

/**
 * Created by Mika on 03.04.2018.
 */

public class MessageReceiver {
    private Connection connection;
    private Server server;
    private Thread receiverThread;

    private boolean stopReceiving = false;

    public MessageReceiver(Connection connection, Server server){
        this.connection = connection;
        this.server = server;
    }

    private Runnable receive = new Runnable() {
        @Override
        public void run() {
            String msg = null;
            try {
                while ((msg = connection.readLine()) != null && !stopReceiving) {
                    Log.i(MainActivity.TAG, "MessageReceiver: Received " + msg);
                    ReceivedMessageHandler handler = MessageHandlerFactory.getHandler(msg, server.id);
                    if(handler != null)
                       handler.handleMessage();
                    else
                       Log.e(MainActivity.TAG, "Received unknown message " + msg);
                }
                Log.i(MainActivity.TAG, "MessageReceiver: Stopped receiving, last message was: " + msg + "; stopReceiving=" + stopReceiving);
            }
            catch (Exception e){e.printStackTrace();}
            finally {
                Log.i(MainActivity.TAG, "MessageReceiver: Connection to server " + server.name + " lost");
                connection.dispose();
                ServerList.getInstance().listeners.serverDisconnected(server);
            }
        }
    };

    public void startReceiveing(){
        try {
            Thread.sleep(25);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        receiverThread = new Thread(receive);
        receiverThread.start();
    }

    public void stopReceiveing(){
        stopReceiving = true;
    }
}

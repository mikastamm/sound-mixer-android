package mikastamm.com.soundmixer.Networking;

import android.util.Log;

import java.io.IOException;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.MessageHandlers.ReceivedMessageHandler;
import mikastamm.com.soundmixer.ServerList;
import mikastamm.com.soundmixer.ServerState;

/**
 * Created by Mika on 03.04.2018.
 */

public class MessageReceiver {
    private ServerConnection connection;
    private Server server;
    private Thread receiverThread;

    private boolean stopReceiving = false;

    public MessageReceiver(ServerConnection connection, Server server){
        this.connection = connection;
        this.server = server;
    }

    private Runnable receive = new Runnable() {
        @Override
        public void run() {
            String msg = null;
            try {
                while ((msg = connection.readLine()) != null && !stopReceiving) {
                   ReceivedMessageHandler handler = MessageHandlerFactory.getHandler(msg, server.id);
                   if(handler != null)
                       handler.handleMessage();
                   else
                       Log.e(MainActivity.TAG, "Received unknown message " + msg);
                }
            }
            catch (IOException e){e.printStackTrace();}
            finally {
                Log.i(MainActivity.TAG, "Connection to server " + server.name + " lost");
                connection.dispose();
                ServerList.getInstance().listeners.onServerDisconnected(server);
            }
        }
    };

    public void startReceiveing(){
        receiverThread = new Thread(receive);
        receiverThread.start();
    }

    public void stopReceiveing(){
        stopReceiving = true;
    }
}

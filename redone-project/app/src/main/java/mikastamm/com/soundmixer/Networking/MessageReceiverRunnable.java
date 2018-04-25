package mikastamm.com.soundmixer.Networking;

import android.util.Log;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.MessageHandlers.ReceivedMessageHandler;
import mikastamm.com.soundmixer.ServerList;

/**
 * Created by Mika on 03.04.2018.
 */

public class MessageReceiverRunnable implements Runnable {
    private Connection connection;
    private Server server;

    private boolean stopReceiving = false;

    public MessageReceiverRunnable(Connection connection, Server server) {
        this.connection = connection;
        this.server = server;
    }

    @Override
    public void run() {
        String msg = null;
        try {
            while ((msg = connection.readLine()) != null && !stopReceiving) {
                Log.i(MainActivity.TAG, "MessageReceiver: Received " + msg);
                ReceivedMessageHandler handler = MessageHandlerFactory.getHandler(msg, server.id);
                if (handler != null)
                    handler.handleMessage();
                else
                    Log.e(MainActivity.TAG, "Received unknown message " + msg);
            }
            Log.i(MainActivity.TAG, "MessageReceiver: Stopped receiving, last message was: " + msg + "; stopReceiving=" + stopReceiving);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.i(MainActivity.TAG, "MessageReceiver: Connection to server " + server.name + " lost");
            connection.dispose();
            ServerList.getInstance().listeners.serverDisconnected(server);
        }
    }
}

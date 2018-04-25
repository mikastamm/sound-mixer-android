package mikastamm.com.soundmixer.Networking;

import android.app.Activity;
import android.util.Log;

import mikastamm.com.soundmixer.ClientAudioSessionsManager;
import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.Helpers.NewThreadRunnable;
import mikastamm.com.soundmixer.Helpers.QueuedRunnable;
import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.MessageSenders.AudioSessionChangeMessageSenderRunnable;
import mikastamm.com.soundmixer.Networking.MessageSenders.DeviceInfoSenderRunnable;
import mikastamm.com.soundmixer.Networking.MessageSenders.RequestAudioSessionsMessageSender;
import mikastamm.com.soundmixer.Networking.MessageSenders.TrackEndMessageSenderRunnable;
import mikastamm.com.soundmixer.Networking.MessageSenders.TrackStartMessageSenderRunnable;
import mikastamm.com.soundmixer.ServerList;
import mikastamm.com.soundmixer.ServerListeners;

/**
 * Created by Mika on 03.04.2018.
 */

public class ServerLogic {
    private Server server;
    private ServerConnection connection = null;
    private ServerListeners.ServerStateChangeListener serverStateChangeListener;
    private Activity activity;

    public ServerLogic(Server server, Activity activity){
        this.server = server;
        this.activity = activity;
    }

    public void dispose(){
        ServerList.getInstance().listeners.removeServerStateChangeListener(serverStateChangeListener);
        activity = null;
    }

    public void connectAndStartCommunicating(){
        connect();
        ClientAudioSessionsManager.registerClientAudioSessions(server.id);
        initListener();

        QueuedRunnable senders = new QueuedRunnable(new DeviceInfoSenderRunnable(connection));
        senders.addRunnable(new RequestAudioSessionsMessageSender(connection));
        senders.addRunnable(new MessageReceiverRunnable(connection, server));
        senders.runInNewThread();
    }

    private void initListener(){
        serverStateChangeListener = new ServerListeners.ServerStateChangeListener() {
            @Override
            public void onActiveServerChanged(Server oldActive, Server newActive) {
                Log.i(MainActivity.TAG, "Active server changed from " + (oldActive == null ? "NULL" : oldActive.name) + " to " + (newActive == null ? "NULL" : newActive.name));

                //If the active server is changed to another server this instance is no longer needed
                if(oldActive != null && oldActive.id.equals(server.id))
                {
                    dispose();
                }
            }

            @Override public void onServerDisconnected(Server server) {
                if(server.id.equals(ServerLogic.this.server.id))
                {
                    ServerList.getInstance().removeActiveServer();
                }
            }

            @Override public void onServerConnected(Server server) {}
        };
        ServerList.getInstance().listeners.addServerStateChangeListener(serverStateChangeListener);
    }

    public void sendAudioSessionChanged(AudioSession changedSession){
        new NewThreadRunnable(new AudioSessionChangeMessageSenderRunnable(changedSession, connection)).run();
    }

    public void sendTrackStart(String sessionId)
    {
        new NewThreadRunnable(new TrackStartMessageSenderRunnable(sessionId, connection)).run();
    }

    public void sendTrackEnd(String sessionId)
    {
        new NewThreadRunnable(new TrackEndMessageSenderRunnable(sessionId, connection)).run();
    }

    private void connect(){
        //If a ServerConnection already exists use it else establish a new connection
        connection = ServerConnectionList.getInstance().get(server.id);
        if(connection == null) {
            connection = new ConnectionFactory(ConnectionFactory.ConnectionType.Socket).makeConnection(server);
            ServerConnectionList.getInstance().add(connection);
        }

            //Connect on new Thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connection.connect();
                }
            }).start();


        ServerList.getInstance().setActiveServer(server);

        if(connection.isConnected()) {
            ServerList.getInstance().listeners.serverConnected(server);
            Log.i(MainActivity.TAG, "Server " + server.name + " now connected & active");
        }
    }


}

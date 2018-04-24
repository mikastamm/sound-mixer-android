package mikastamm.com.soundmixer.Networking;

import android.app.Activity;
import android.util.Log;

import mikastamm.com.soundmixer.ClientAudioSessionsManager;
import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.KnownServerList;
import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.MessageSenders.AudioSessionChangeMessageSender;
import mikastamm.com.soundmixer.Networking.MessageSenders.DeviceInfoSender;
import mikastamm.com.soundmixer.Networking.MessageSenders.RequestAudioSessionsMessageSender;
import mikastamm.com.soundmixer.Networking.MessageSenders.TrackEndMessageSender;
import mikastamm.com.soundmixer.Networking.MessageSenders.TrackStartMessageSender;
import mikastamm.com.soundmixer.ServerList;
import mikastamm.com.soundmixer.ServerListeners;

/**
 * Created by Mika on 03.04.2018.
 */

public class ServerLogic {
    private Server server;
    private ServerConnection connection = null;
    private MessageReceiver receiver;
    private ServerListeners.ServerStateChangeListener serverStateChangeListener;
    private Activity activity;

    public ServerLogic(Server server, Activity activity){
        this.server = server;
        this.activity = activity;
    }

    public void dispose(){
        receiver.stopReceiveing();
        ServerList.getInstance().listeners.removeServerChangeListener(serverStateChangeListener);
        activity = null;
    }

    public void connectAndStartCommunicating(){
        connect();
        ClientAudioSessionsManager.registerClientAudioSessions(server.id);
        receiver = new MessageReceiver(connection, server);

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

        DeviceInfoSender deviceInfoSender = new DeviceInfoSender(connection);
        deviceInfoSender.addSender(new RequestAudioSessionsMessageSender(connection));
        deviceInfoSender.send();

        //Receive in new Thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                receiver.startReceiveing();
            }
        }).start();
        KnownServerList.setLastConnectedServer(server.id, activity);
    }

    public void sendAudioSessionChanged(AudioSession changedSession){
        AudioSessionChangeMessageSender s = new AudioSessionChangeMessageSender(changedSession, connection);
        s.send();
    }

    public void sendTrackStart(String sessionId)
    {
        TrackStartMessageSender s = new TrackStartMessageSender(sessionId, connection);
        s.send();
    }

    public void sendTrackEnd(String sessionId)
    {
        TrackEndMessageSender s = new TrackEndMessageSender(sessionId, connection);
        s.send();
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

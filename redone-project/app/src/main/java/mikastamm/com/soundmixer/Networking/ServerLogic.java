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
import mikastamm.com.soundmixer.ServerStateChangeDelegate;

/**
 * Created by Mika on 03.04.2018.
 */

public class ServerLogic {
    private Server server;
    private ServerConnection connection = null;
    private ServerStateChangeDelegate.ServerStateChangeListener serverStateChangeListener;

    public ServerLogic(Server server, Activity activity) {
        this.server = server;
    }

    public void dispose() {
        ServerList.getInstance().stateChangeDelegate.removeServerStateChangeListener(serverStateChangeListener);
    }

    public void connectAndStartCommunicating() {
        if(isAlreadyConnected())
            recoverConnection();
        else
            makeNewConnection();

        ClientAudioSessionsManager.registerClientAudioSessions(server.id);
        initListener();

        QueuedRunnable senders = new QueuedRunnable(new DeviceInfoSenderRunnable(connection));
        senders.addRunnable(new RequestAudioSessionsMessageSender(connection));
        senders.addRunnable(new MessageReceiverRunnable(connection, server));
        senders.runInNewThread();
    }

    private void initListener() {
        serverStateChangeListener = new ServerStateChangeDelegate.ServerStateChangeListener() {
            @Override
            public void onActiveServerChanged(Server oldActive, Server newActive) {
                Log.i(MainActivity.TAG, "Active server changed from " + (oldActive == null ? "NULL" : oldActive.name) + " to " + (newActive == null ? "NULL" : newActive.name));

                //If the active server is changed to another server this instance is no longer needed
                if (oldActive != null && oldActive.id.equals(server.id)) {
                    dispose();
                }
            }

            @Override
            public void onServerDisconnected(Server server) {
                if (server.id.equals(ServerLogic.this.server.id)) {
                    ServerList.getInstance().removeActiveServer();
                    ServerConnectionList.getInstance().disconnectAndRemove(server.id);
                }
            }

            @Override
            public void onServerConnected(Server server) {
            }
        };
        ServerList.getInstance().stateChangeDelegate.addServerStateChangeListener(serverStateChangeListener);
    }

    public void sendAudioSessionChanged(AudioSession changedSession) {
        new NewThreadRunnable(new AudioSessionChangeMessageSenderRunnable(changedSession, connection)).run();
    }

    public void sendTrackStart(String sessionId) {
        new NewThreadRunnable(new TrackStartMessageSenderRunnable(sessionId, connection)).run();
    }

    public void sendTrackEnd(String sessionId) {
        new NewThreadRunnable(new TrackEndMessageSenderRunnable(sessionId, connection)).run();
    }

    private void makeNewConnection() {
        connection = new ConnectionFactory(ConnectionFactory.ConnectionType.Socket).makeConnection(server);
        ServerConnectionList.getInstance().add(connection);
        //Connect on new Thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                connection.connect();
            }
        }).start();
        checkIfConnectedAndRaiseEvents();
    }

    private void recoverConnection() {
        connection = ServerConnectionList.getInstance().get(server.id);
        checkIfConnectedAndRaiseEvents();
    }

    private void checkIfConnectedAndRaiseEvents(){
        if (connection.isConnected()) {
            ServerList.getInstance().stateChangeDelegate.serverConnected(server);
            ServerList.getInstance().setActiveServer(server);
            Log.i(MainActivity.TAG, "Server " + server.name + " now connected & active");
        }
    }

    private boolean isAlreadyConnected() {
        return ServerConnectionList.getInstance().contains(server.id);
    }


}

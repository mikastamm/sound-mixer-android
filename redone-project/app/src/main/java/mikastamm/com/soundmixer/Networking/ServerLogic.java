package mikastamm.com.soundmixer.Networking;

import android.util.Log;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.ServerList;

/**
 * Created by Mika on 03.04.2018.
 */

public class ServerLogic {
    private Server server;
    private ServerConnection connection = null;
    private MessageReceiver receiver;

    public ServerLogic(Server server){
        this.server = server;
    }

    public void dispose(){
        receiver.stopReceiveing();
    }

    public void ConnectAndStartCommunicating(){
        connect();
        receiver = new MessageReceiver(connection, server);
        //Receive in new Thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                receiver.startReceiveing();
            }
        }).start();
    }

    public void connect(){
        //If a ServerConnection already exists use it else establish a new connection
        connection = ServerConnectionList.getInstance().get(server.id);
        if(connection == null) {
            connection = new ServerConnectionFactory(ServerConnectionFactory.ConnectionType.Socket).makeConnection(server);
            ServerConnectionList.getInstance().add(connection);
        }

        if(!connection.isConnected()) {
            //Connect on new Thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connection.connect();
                }
            }).start();
        }

        ServerList.getInstance().setActiveServer(server);

        if(connection.isConnected()) {
            ServerList.getInstance().listeners.onServerConnected(server);
            Log.i(MainActivity.TAG, "Server " + server.name + " now connected & active");
        }
    }


}

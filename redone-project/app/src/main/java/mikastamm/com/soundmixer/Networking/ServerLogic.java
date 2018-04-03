package mikastamm.com.soundmixer.Networking;

import java.io.IOException;

import mikastamm.com.soundmixer.Datamodel.Server;
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

    public ServerLogic(Server server, ServerConnection connection){
        this.server = server;
        this.connection = connection;
    }

    public void dispose(){
        receiver.stopReceiveing();
    }

    public void ConnectAndStartCommunicating(){
        establishConnection();
        receiver = new MessageReceiver(connection, server);
        receiver.startReceiveing();
    }

    public void establishConnection(){
        if(connection == null) {
            connection = new ServerConnectionFactory(ServerConnectionFactory.ConnectionType.Socket).makeConnection(server);
        }

        if(!connection.isConnected())
            connection.connect();

        if(connection.isConnected())
            ServerList.getInstance().listeners.onServerConnected(server);
    }


}

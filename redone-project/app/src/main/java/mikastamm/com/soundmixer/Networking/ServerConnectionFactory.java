package mikastamm.com.soundmixer.Networking;

import mikastamm.com.soundmixer.Datamodel.Server;

/**
 * Created by Mika on 03.04.2018.
 */

public class ServerConnectionFactory {
    private ConnectionType type;

    public ServerConnectionFactory(ConnectionType type)
    {
        this.type = type;
    }

    public ServerConnection makeConnection(Server server)
    {
        if(type == ConnectionType.Socket)
        {
            SocketServerConnection conn = new SocketServerConnection(server.IPAddress);
            return conn;
        }
        return null;
    }

    public enum ConnectionType{
        Socket
    }
}

package mikastamm.com.soundmixer.Networking;

import java.net.Socket;

import mikastamm.com.soundmixer.Datamodel.Server;

/**
 * Created by Mika on 03.04.2018.
 */

public class ConnectionFactory {
    private ConnectionType type;

    public ConnectionFactory()
    {
        this.type = ConnectionType.Socket;
    }

    public ConnectionFactory(ConnectionType type)
    {
        this.type = type;
    }

    public ServerConnection makeConnection(Server server)
    {
        if(type == ConnectionType.Socket)
        {
            return new SocketServerConnection(server);
        }
        return null;
    }

    public Connection makeConnection(String ip)
    {
        if(type == ConnectionType.Socket) {
            return new SocketConnection(ip);
        }
        return null;
    }

    public Connection makeConnection(Socket socket)
    {
        if(type == ConnectionType.Socket) {
            return new SocketConnection(socket);
        }
        return null;
    }

    public enum ConnectionType{
        Socket
    }
}

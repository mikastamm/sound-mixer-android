package mikastamm.com.soundmixer.Networking;

import java.net.Socket;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.Exceptions.InvalidConfigurationException;

/**
 * Created by Mika on 03.04.2018.
 */

public class ConnectionFactory {
    private boolean useSsl = false;

    public ConnectionFactory(){}

    public ConnectionFactory(boolean useSsl)
    {
        this.useSsl = useSsl;
    }

    public ServerConnection makeConnection(Server server)
    {
        if(!useSsl)
            return new SocketServerConnection(server);
        else
            return new SslServerConnection(server);
    }

    public Connection makeConnection(String ip)
    {
        if(!useSsl)
            return new SocketConnection(ip);
        else
            return new SslConnection(ip);
    }

    public Connection makeConnection(Socket socket)
    {
        if(useSsl)
            throw new InvalidConfigurationException("Cannot use ssl with a socket");

        return new SocketConnection(socket);
    }

    public enum ConnectionType{
        Socket
    }
}

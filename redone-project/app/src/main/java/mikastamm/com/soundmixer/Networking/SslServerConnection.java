package mikastamm.com.soundmixer.Networking;

import javax.net.ssl.SSLSocket;

import mikastamm.com.soundmixer.Datamodel.Server;

/**
 * Created by Mika on 03.05.2018.
 */

public class SslServerConnection extends SslConnection implements ServerConnection {
    private Server server;

    public SslServerConnection(Server server)
    {
        super(server.ipAddress);
        this.server = server;
    }

    @Override
    public Server getServer() {
        return server;
    }
}

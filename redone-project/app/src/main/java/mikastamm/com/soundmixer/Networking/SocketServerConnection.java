package mikastamm.com.soundmixer.Networking;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.MainActivity;

/**
 * Created by Mika on 28.03.2018.
 */

public class SocketServerConnection extends SocketConnection implements ServerConnection  {
    private Server server;

    public SocketServerConnection(Server server)
    {
        super(server.ipAddress);
        this.server = server;
    }
    public SocketServerConnection(Server server, Socket socket){
        super(socket);
        this.server = server;
    }

    @Override
    public Server getServer(){
        return server;
    }
}

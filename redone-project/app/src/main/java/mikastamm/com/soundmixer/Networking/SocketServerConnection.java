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

public class SocketServerConnection implements ServerConnection {
    private Server server;

    private Socket socket;
    private BufferedReader inFromServer;
    private OutputStream outstream;
    private PrintWriter outWriter;

    private boolean connected = false;

    public SocketServerConnection(Server server)
    {
        this.server = server;
    }
    public SocketServerConnection(Server server, Socket socket){this.socket = socket; this.server = server;}


    @Override
    public Server getServer(){
        return server;
    }

    @Override
    public void writeLine(String line) {
        if(outWriter != null) {
            outWriter.println(line);
            outWriter.flush();
        }
    }

    @Override
    public String readLine() throws IOException {
        if(inFromServer != null)
            return inFromServer.readLine();
        else
            return null;
    }

    @Override
    public void connect() {
        Log.i(MainActivity.TAG, "Establishing connection to " + server.ipAddress);
        try{
            if(socket == null || socket.isClosed())
            socket = new Socket(InetAddress.getByName(server.ipAddress), Constants.SERVER_CONNECTION_TCP_PORT);

            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outstream = socket.getOutputStream();
            outWriter = new PrintWriter(outstream);
            connected = true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        connected = false;

        if(outWriter != null) {
            outWriter.flush();
            outWriter.close();
        }

        try {
            socket.close();
            inFromServer.close();
            outstream.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isConnected(){
        return connected;
    }


}

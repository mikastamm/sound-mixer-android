package mikastamm.com.soundmixer.Networking;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Mika on 28.03.2018.
 */

public class SocketServerConnection implements ServerConnection {

    private String ipAddress;

    private Socket socket;
    private BufferedReader inFromServer;
    private OutputStream outstream;
    private PrintWriter outWriter;

    private boolean connected = false;

    public SocketServerConnection(String ipAddress)
    {
        this.ipAddress = ipAddress;
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
        Log.i(this.getClass().toString(), "Establishing connection to " + ipAddress);
        try{
            socket = new Socket(InetAddress.getByName(ipAddress), Constants.SERVER_CONNECTION_TCP_PORT);
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
        Log.i(this.getClass().toString(), "Closing connection to " + ipAddress);
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

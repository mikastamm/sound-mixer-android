package mikastamm.com.soundmixer.Networking;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import mikastamm.com.soundmixer.MainActivity;

/**
 * Created by Mika on 17.04.2018.
 */

public class SocketConnection implements Connection {
    private String ipAddress;

    private Socket socket;
    private BufferedReader inFromServer;
    private OutputStream outstream;
    private PrintWriter outWriter;

    private boolean connected = false;

    public SocketConnection(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public SocketConnection(Socket socket) {
        this.socket = socket;
        this.ipAddress = socket.getInetAddress().getHostAddress();
    }


    @Override
    public void writeLine(String line) {
        if (outWriter != null) {
            outWriter.println(line);
            outWriter.flush();
        }
    }

    @Override
    public String readLine() throws IOException {
            return inFromServer.readLine();
    }

    @Override
    public void connect() {
        Log.i(MainActivity.TAG, "Establishing connection to " + ipAddress);
        try {
            if (socket == null || socket.isClosed())
                socket = new Socket(InetAddress.getByName(ipAddress), Constants.SERVER_CONNECTION_TCP_PORT);

            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outstream = socket.getOutputStream();
            outWriter = new PrintWriter(outstream);
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };

    @Override
    public void dispose() {
        connected = false;

        if (outWriter != null) {
            outWriter.flush();
            outWriter.close();
        }

        try {
            socket.close();
            inFromServer.close();
            outstream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
}

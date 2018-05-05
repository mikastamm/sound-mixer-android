package mikastamm.com.soundmixer.Networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Mika on 03.05.2018.
 */

public class SslConnection implements Connection {

    private String ipAddress;
    private SSLSocket socket;

    private BufferedReader inFromServer;
    private OutputStream outstream;
    private PrintWriter outWriter;

    private boolean connected = false;

    public SslConnection(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    @Override
    public void connect() {
        SocketFactory sf = SSLSocketFactory.getDefault();
        try {
            socket = (SSLSocket) sf.createSocket(ipAddress, Constants.SERVER_CONNECTION_TCP_PORT);
            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outstream = socket.getOutputStream();
            outWriter = new PrintWriter(outstream);

            connected = true;
        }catch (IOException e)
        {
            e.printStackTrace();
        }
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

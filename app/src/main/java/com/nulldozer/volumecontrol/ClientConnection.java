package com.nulldozer.volumecontrol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Mika on 07.11.2017.
 */

public class ClientConnection {
    public boolean connected;
    public Socket socket;
    public BufferedReader inFromServer;
    public OutputStream outstream;
    public PrintWriter outWriter;

    public VolumeServer activeServer;

    public ClientConnection(VolumeServer activeServer)
    {
        this.activeServer = activeServer;
    }

    public void send(final String data)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(outWriter != null) {
                    outWriter.println(data);
                    outWriter.flush();
                }
            }
        }).start();
    }

    public void connect() throws IOException{
            socket = new Socket(InetAddress.getByName(activeServer.IPAddress), Constants.SERVER_CONNECTION_TCP_PORT);
            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outstream = socket.getOutputStream();
            outWriter = new PrintWriter(outstream);
            connected = true;
    }

    public void close(){
        if(outWriter != null) {
            outWriter.flush();
            outWriter.close();
        }

        try {
            socket.close();
            inFromServer.close();
            outstream.close();
            activeServer.active = false;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


}

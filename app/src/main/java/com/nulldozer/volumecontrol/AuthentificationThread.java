package com.nulldozer.volumecontrol;


import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Mika on 24.08.2017.
 */
public class AuthentificationThread implements Runnable{

    public VolumeServer server;
    public String password;

    public AuthentificationThread(VolumeServer server, String password){
        this.server = server;
        this.password = password;
    }

    @Override
    public void run() {

        try {
            if(server.hasPassword) {
                Socket socket = new Socket(server.IPAddress, MainActivity.SERVER_CONNECTION_TCP_PORT);
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream());


            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }

    public String getDeviceID(){
            return ((WifiManager) MainActivity.Instance.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress();
    }

}

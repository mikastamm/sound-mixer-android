package com.nulldozer.volumecontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Mika on 18.08.2017.
 */
public class NetworkDiscoveryThread implements Runnable {

    private MainActivity main;
    private final String TAG = "NetworkDiscoveryThread";
    private Thread t;
    private static Set<VolumeServer> servers = Collections.synchronizedSet(new HashSet<VolumeServer>());
    public final long SERVER_SEARCH_TIMEOUT_IN_MILLISECONDS = (int)(Constants.maximalResponseTimeForServerInSeconds * 1000);
    private boolean silent = false;


    public NetworkDiscoveryThread() {
        main = MainActivity.Instance;
    }
    public NetworkDiscoveryThread(boolean silent){
        main = MainActivity.Instance;
        this.silent = silent;
    }

    @Override
    public void run() {
        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.serverListViewAdapter.listElements.clear();
                main.networkEventHandlers.onNetworkDiscoveryStarted(silent);
                main.serverListViewAdapter.notifyDataSetChanged();
            }
        });

        sendBroadcast("VC_HELLO");
        receiveResponse();
    }

    public static Set<VolumeServer> getServers(){
        return servers;
    }

    public void receiveResponse() {

        Date endDate = Calendar.getInstance().getTime();
        endDate.setTime(endDate.getTime() + SERVER_SEARCH_TIMEOUT_IN_MILLISECONDS);
        int serverCount = servers.size();

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(Constants.NETWORK_DISCOVERY_TCP_PORT);

            while (Calendar.getInstance().getTime().getTime() < endDate.getTime()) {
                serverSocket.setSoTimeout((int) (endDate.getTime() - Calendar.getInstance().getTime().getTime()));
                Socket s = serverSocket.accept();

                GetNewServerInfoThread getNewServerInfoThread = new GetNewServerInfoThread();
                getNewServerInfoThread.start(serverCount, s, servers);

                serverCount++;
            }


        } catch (IOException ioe) {
            Log.i(TAG, "Network Discovery Finished (Timeout)");

            //Call event on Main-Thread
            main.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    main.networkEventHandlers.onNetworkDiscoveryFinished();
                }
            });
        }
        finally {
            try {
                if (serverSocket != null)
                    serverSocket.close();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }

    public void sendBroadcast(String messageStr) {
        // Hack Prevent crash (sending should be done using an async task)
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] sendData = messageStr.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getBroadcastAddress() , Constants.NETWORK_DISCOVERY_UDP_PORT);
            socket.send(sendPacket);
            System.out.println(getClass().getName() + "Broadcast packet sent to: " + getBroadcastAddress().getHostAddress());
        } catch (IOException e) {
            Log.e("TAG", "IOException: " + e.getMessage());
        }
        finally {
            if(socket != null)
            socket.close();
        }
    }

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) main.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    public void start()
    {
        System.out.println("Starting " + TAG);
        if (t == null)
        {
            t = new Thread (this, TAG);
            t.start ();
        }
    }

    class GetNewServerInfoThread implements Runnable{
        private Thread t;
        private Socket socket;

        @Override
        public void run() {
            String serverData = "";

            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                serverData = reader.readLine();
                Log.i("ReceivedFromServer", serverData);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if(!serverData.equals(""))
            {
                final VolumeServer server = JSONManager.deserialize(serverData, VolumeServer.class);
                if(server != null) {
                server.IPAddress = socket.getRemoteSocketAddress().toString();

                main.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.Instance.networkEventHandlers.onServerDiscovered(server);
                    }
                });
            }

            }
        }

        public void start(int serverNr, Socket socket, Set<VolumeServer> servers)
        {
            this.socket = socket;
            String threadName = "HandleNewServerThread" + serverNr;
            System.out.println("Starting " +  threadName );

            if (t == null)
            {
                t = new Thread (this, threadName);
                t.start ();
            }
            else
            {
                Log.i(TAG, "Thread Already Running");
            }

        }
    }
}



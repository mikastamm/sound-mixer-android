package com.nulldozer.volumecontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.StrictMode;
import android.provider.SyncStateContract;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Mika on 18.08.2017.
 */
public class NetworkDiscoveryThread implements Runnable {

    private MainActivity main;
    private final String TAG = "NetworkDiscoveryThread";
    private Thread t;
    private static Set<VolumeServer> servers = Collections.synchronizedSet(new HashSet<VolumeServer>());
    public final long SERVER_SEARCH_TIMEOUT_IN_MILLISECONDS = (int)(MainActivity.Instance.maximalResponseTimeForServerInSeconds * 1000);
    String activeVolumeServerRSAKey;

    public NetworkDiscoveryThread() {
        main = MainActivity.Instance;

        activeVolumeServerRSAKey = main.serverListViewAdapter.getActive();
    }

    @Override
    public void run() {
        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.serverListViewAdapter.listElements.clear();
                main.onNetworkDiscoveryStarted();
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
            serverSocket = new ServerSocket(MainActivity.NETWORK_DISCOVERY_TCP_PORT);

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
                    main.onNetworkDiscoveryFinished();
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
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getBroadcastAddress() , MainActivity.NETWORK_DISCOVERY_UDP_PORT);
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
        WifiManager wifi = (WifiManager) main.getSystemService(Context.WIFI_SERVICE);
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
                try {
                    final VolumeServer server = JSONManager.deserialize(serverData, VolumeServer.class); //TESTCASE: invalid json
                    server.IPAddress = socket.getRemoteSocketAddress().toString();

                    if(server.RSAPublicKey == activeVolumeServerRSAKey)
                    {
                        server.active = true;
                    }

                    final SharedPreferences prefs = MainActivity.Instance.getPreferences(MainActivity.MODE_PRIVATE);
                    String stdPassword = prefs.getString(MainActivity.ServerStandardPasswordPrefix_PrefKey + VCCryptography.getMD5Hash(server.RSAPublicKey), "");
                    Log.i(TAG, "Saved Standard password = " + stdPassword);

                    server.standardPassword = stdPassword;


                    if(server.IPAddress.indexOf(":") != -1)
                    {
                        server.IPAddress = server.IPAddress.substring(0, server.IPAddress.indexOf(":"));
                    }

                    if(server.IPAddress.startsWith("/"))
                    {
                        server.IPAddress=server.IPAddress.substring(1);
                    }

                    main.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.Instance.onServerDiscovered(server);
                        }
                    });

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(MainActivity.FirstConnectHappened_PrefKey, true);
                    editor.apply();

                    //Connect to the found Server if not connected and allowed in settings
                    if(MainActivity.Instance.serverListViewAdapter.activeServer == null)
                    {
                        MainActivity.Instance.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                VolumeServer lastConnected;
                                String lastConnectedRsaKey;
                                VolumeServer passwordLessServer;

                                lastConnectedRsaKey = prefs.getString(MainActivity.LastConnectedServer_PrefKey, null);

                                if(MainActivity.autoConnectToLastConnectedServer && lastConnectedRsaKey != null && (lastConnected = MainActivity.Instance.serverListViewAdapter.getItem(lastConnectedRsaKey)) != null){
                                    MainActivity.Instance.serverListViewAdapter.setActive(lastConnected);
                                }
                                else if(MainActivity.autoConnectToServersWithoutPassword && (passwordLessServer = MainActivity.Instance.serverListViewAdapter.getPasswordlessServer()) != null)
                                {
                                    MainActivity.Instance.serverListViewAdapter.setActive(passwordLessServer);
                                }
                            }
                        });
                    }
                }
                catch(RuntimeException e) {
                    e.printStackTrace();
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



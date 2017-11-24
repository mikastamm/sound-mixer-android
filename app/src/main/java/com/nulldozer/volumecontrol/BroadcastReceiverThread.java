package com.nulldozer.volumecontrol;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Mika on 04.11.2017.
 */

public class BroadcastReceiverThread implements Runnable {

    private final static String TAG = "BroadcastReceiverThread";
    private DatagramSocket socket;
    private Thread t;
    private MainActivity main;

    public void close(){
        socket.close();
        main = null;
    }

    @Override
    public void run(){
        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            socket = new DatagramSocket(Constants.REVERSE_DISCOVERY_UDP_PORT, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            while (!socket.isClosed()) {
                try {
                    Log.i(TAG, "Ready to receive broadcast packets!");

                    //Receive a packet
                    byte[] recvBuf = new byte[15000];
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.receive(packet);

                    if(socket.isClosed())
                        return;

                    //Packet received
                    Log.i(TAG, "Packet received from: " + packet.getAddress().getHostAddress());
                    String data = new String(packet.getData()).trim();
                    Log.i(TAG, "Packet received; data: " + data);
                    if(data.equals("VCHELLO"))
                    {
                        new NetworkDiscoveryThread(true, main).start();
                    }
                }
                catch (IOException ex)
                {
                    Log.i(TAG, "Exception while trying to receive Broadcast: " + ex.getMessage());
                    try {
                        Thread.sleep(2000);
                    }catch (InterruptedException iex)
                    {
                        iex.printStackTrace();
                    }
                }
            }
        } catch (IOException ex) {
            Log.i(TAG, "Oops" + ex.getMessage());
        }
    }

    public void start(MainActivity main){
        this.main = main;
        String threadName = "BroadCastReceiverThread";
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

package mikastamm.com.soundmixer.Networking.MessageSenders;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.Constants;

/**
 * Created by Mika on 09.04.2018.
 */

public class ListenForServersRunnable implements Runnable {
    private DatagramSocket socket;

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(Constants.REVERSE_DISCOVERY_UDP_PORT, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            while (!socket.isClosed()) {
                try {
                    Log.i(MainActivity.TAG, "Ready to receive broadcast packets!");

                    //Receive a packet
                    byte[] recvBuf = new byte[15000];
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.receive(packet);

                    if(socket.isClosed())
                        return;

                    //Packet received
                    Log.i(MainActivity.TAG, "Packet received from: " + packet.getAddress().getHostAddress());
                    String data = new String(packet.getData()).trim();
                    Log.i(MainActivity.TAG, "Packet received; data: " + data);

                    if(data.equals("VC_HELLO"))
                    {
                        //TODO: Handle
                    }
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                    try {
                        Thread.sleep(2000);
                    }catch (InterruptedException iex)
                    {
                        iex.printStackTrace();
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

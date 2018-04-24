package mikastamm.com.soundmixer.Networking;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Date;

import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.Constants;
import mikastamm.com.soundmixer.Networking.GetInfoFromServerRunnable;

import static android.content.ContentValues.TAG;

/**
 * Created by Mika on 04.04.2018.
 */

public class FindServersRunnable implements Runnable {
    private ServerSocket serverSocket = null;
    public int searchTimeoutMs = 3000;
    private String broadcastMessage = "VC_HELLO";
    private Activity activity;
    private NetworkDiscoveryBroadcastSender.NetworkDiscoveryDelegate delegate;

    public FindServersRunnable(Activity activity, NetworkDiscoveryBroadcastSender.NetworkDiscoveryDelegate delegate)
    {
        this.activity = activity;
        this.delegate = delegate;
    }

    @Override
    public void run() {
        sendBroadcast(broadcastMessage);
        receiveResponse();
    }

    public void stopReceiving(){
        try {
            if (serverSocket != null)
                serverSocket.close();
        }
        catch(IOException e) {/*ignored*/}
    }

    public void receiveResponse() {
        try {
            acceptSocketAndHandleNewConnection();
        } catch (IOException ioe) {
            //Notify the delegate that nd finished
            //Delegate then calls all subscribed listeners
            delegate.networkDiscoveryFinished();
        }
        finally {
            try {
                if (serverSocket != null)
                    serverSocket.close();
            }
            catch(IOException e) {/*ignored*/}
        }
    }

    private void acceptSocketAndHandleNewConnection() throws IOException {
        serverSocket = new ServerSocket(Constants.NETWORK_DISCOVERY_TCP_PORT);

        //Accept new clients until the timeout
        Date endDate = Calendar.getInstance().getTime();
        endDate.setTime(endDate.getTime() + searchTimeoutMs);
        while (Calendar.getInstance().getTime().getTime() < endDate.getTime()) {
            serverSocket.setSoTimeout((int) (endDate.getTime() - Calendar.getInstance().getTime().getTime()));
            try {
                Socket s = serverSocket.accept();
                Log.i(MainActivity.TAG, "Network Discovery: potential client found");
                //Spawn new Thread to Handle the new Connection and Get the Info from the Server
                new Thread(new GetInfoFromServerRunnable(s)).start();
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendBroadcast(String messageStr) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);

            byte[] sendData = messageStr.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getBroadcastAddress() , Constants.NETWORK_DISCOVERY_UDP_PORT);
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(socket != null)
                socket.close();
        }
    }

    private InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        //TODO: handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

        InetAddress broadcastAddr = InetAddress.getByAddress(quads);


        return broadcastAddr;
    }
}

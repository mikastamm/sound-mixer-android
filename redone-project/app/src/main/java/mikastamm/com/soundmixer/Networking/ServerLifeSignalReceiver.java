package mikastamm.com.soundmixer.Networking;

import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static mikastamm.com.soundmixer.MainActivity.TAG;

/**
 * Created by Mika on 09.04.2018.
 */

public class ServerLifeSignalReceiver {

    private DatagramSocket socket;
    private final int maxPacketSizeByte=128;
    private WeakReference<NetworkDiscoveryServerSearcher> searcherRef;

    public ServerLifeSignalReceiver(NetworkDiscoveryServerSearcher serverSearcher)
    {
        searcherRef = new WeakReference<>(serverSearcher);
    }

    public void start() {
        new Thread(receiveRunnable).start();
    }

    public void stop() {
        if (socket != null)
            socket.close();
    }

    private Runnable receiveRunnable = new Runnable() {
        @Override
        public void run() {
            receiveAndHandleErrors();
        }
    };

    private void receiveAndHandleErrors() {
        try {
            receive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receive() throws IOException {
        openSocket();
        Log.i(TAG, "ServerLifeSignalReceiver now receiving");
        while (!socket.isClosed()) {
            String received = readLine();
            Log.i(TAG, "ServerLifeSignalReceiver received " + received);
            if (received.equals("VC_HELLO")) {
                Log.i(TAG, "ServerLifeSignalReceiver Received Broadcast from Server");

                NetworkDiscoveryServerSearcher serverSearcher = searcherRef.get();
                if(serverSearcher != null)
                    serverSearcher.searchForServers();
            }
        }
    }

    private String readLine() throws IOException{
        byte[] buffer = new byte[maxPacketSizeByte];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData()).trim();
    }

    private void openSocket() throws IOException{
        socket = new DatagramSocket(Constants.REVERSE_DISCOVERY_UDP_PORT, InetAddress.getByName("0.0.0.0"));
        socket.setBroadcast(true);
    }
}

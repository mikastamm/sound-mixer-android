package com.nulldozer.volumecontrol;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;


public class ClientThread{

    public boolean connected;

    public String currentMessage;

    private Socket socket;
    private BufferedReader inFromServer;
    private OutputStream outstream;
    private PrintWriter outWriter;

    VolumeServer activeServer;

    public Thread listenerThread;

    private final String TAG = "ClientThread";

    public ClientThread(){
        if(MainActivity.Instance.serverListViewAdapter.activeServer != null) {
            activeServer = MainActivity.Instance.serverListViewAdapter.activeServer;
        }
        else{
            activeServer = new VolumeServer(false, "", "");
        }
        Thread connectThread = new Thread(connectToDataPort);
        connectThread.start();
    }

    public void close(){
        Log.i(TAG, "Closing client Thread");
        if(outWriter != null) {
            outWriter.flush();
            outWriter.close();
        }

        try {
            socket.close();
            inFromServer.close();
            outstream.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        if(listenerThread != null)
        listenerThread.interrupt();

        MainActivity.Instance.adapter.clear();
    }

    public void send(String data)
    {
        try {
            if(outWriter != null) {
                byte[] encryptedData = VCCryptography.encrypt((activeServer.hasPassword ? activeServer.getHashedPassword() + ";" : "") + data, activeServer.RSAPublicKey);
                if (encryptedData != null) {
                    byte[] base64Data = Base64.encode(encryptedData, Base64.NO_WRAP);
                    String encryptedString = new String(base64Data, "UTF-8");
                    outWriter.println(encryptedString);
                    outWriter.flush();
                }
            }
        }
        catch (UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
        }
    }

    public Runnable connectToDataPort = new Runnable() {
        @Override
        public void run() {
            try {
                Log.i(TAG, "Active Server IP: " + activeServer.IPAddress);
                socket = new Socket(InetAddress.getByName(activeServer.IPAddress), MainActivity.SERVER_CONNECTION_TCP_PORT);
                inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outstream = socket.getOutputStream();
                outWriter = new PrintWriter(outstream);
                connected = true;
                Log.i(TAG, "Connected to Server: " + activeServer.IPAddress);

                ClientDevice thisDevice = new ClientDevice();
                thisDevice.Name = Build.MODEL;
                thisDevice.RSAKeyJSON = VCCryptography.getRSAPublicKeyJSON();
                thisDevice.Version = BuildConfig.VERSION_CODE;

                outWriter.println("DEVINFO" + JSONManager.serialize(thisDevice));
                outWriter.flush();

                MainActivity.Instance.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout llConnectionTip = (LinearLayout)MainActivity.Instance.findViewById(R.id.llConnectionTip);
                        llConnectionTip.setVisibility(View.GONE);
                    }
                });

                listenerThread = new Thread(receiveAudioData);
                listenerThread.start();

                try{
                    Thread.sleep(1000);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
                requestAllAudioSessionsFromServer.run();
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
                connected = false;
            }
        }
    };

    public void sendVolumeData(VolumeData data)
    {
        new Thread(new SendVolumeDataRunnable(data)).start();
    }

    public void startTracking(VolumeData data){
        new Thread(new SendStartTrackingRunnable(data)).start();
    }

    public void endTracking(VolumeData data){
        new Thread(new SendEndTrackingRunnable(data)).start();
    }

    public class SendVolumeDataRunnable implements Runnable
    {
        VolumeData data;
        SendVolumeDataRunnable(VolumeData data){this.data = data;}

        @Override
        public void run() {
            if(connected && socket != null && outWriter != null)
            {
                String toSend = JSONManager.serialize(data);
                send("EDIT"+toSend);

                Log.i(TAG, "Sent Volume Change:" + toSend);
            }
            else{
                Log.i(TAG, "Not connected or socket/outWriter is null");
            }
        }
    }

    public class SendStartTrackingRunnable implements Runnable
    {
        VolumeData data;
        SendStartTrackingRunnable(VolumeData data){this.data = data;}

        @Override
        public void run() {
            if(connected && socket != null && outWriter != null)
            {
                send("TRACK"+data.id);


                Log.i(TAG, "Sent tracking Started");
            }
            else{
                Log.i(TAG, "Not connected or socket/outWriter is null");
            }
        }
    }

    public class SendEndTrackingRunnable implements Runnable
    {
        VolumeData data;
        SendEndTrackingRunnable(VolumeData data){this.data = data;}

        @Override
        public void run() {
            if(connected && socket != null && outWriter != null)
            {
                send("ENDTRACK"+data.id);

                Log.i("ClientThread", "Sent tracking End");
            }
            else{
                Log.i("ClientThread", "Not connected or socket/outWriter is null");
            }
        }
    }


    public Runnable requestAllAudioSessionsFromServer = new Runnable() {
        @Override
        public void run() {
            if(connected && socket != null && outWriter != null)
            {
                send("GETAUDIOSESSIONS");

                Log.i("ClientThread", "Requested all audio sessions");
            }
            else{
                Log.i("ClientThread", "Not connected or socket/outWriter is null");
            }
        }
    };

    public Runnable receiveAudioData = new Runnable() {
        @Override
        public void run() {
            if(connected && socket != null && outWriter != null && inFromServer != null) {
                Log.i("ClientThread", "Now receiving AudioData...");
                boolean endOfStream = false;
                while (!Thread.interrupted() && !endOfStream) {
                    try {
                        currentMessage = inFromServer.readLine();

                        if (currentMessage != null && !Thread.interrupted()) {
                            final String msg = currentMessage;//VCCryptography.getDecryptedMessage(currentMessage);
                            Log.i("ClientThread", "received: " + msg);
                            if (msg.startsWith("REP")) { //Repopulate Data set (Clear & Set)
                                SharedPreferences.Editor editor = MainActivity.Instance.getPreferences(MainActivity.MODE_PRIVATE).edit();
                                editor.putString(MainActivity.LastConnectedServer_PrefKey, activeServer.RSAPublicKey);
                                editor.apply();
                                MainActivity.Instance.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!MainActivity.Instance.fragmentRetained)
                                        Toast.makeText(MainActivity.Instance, "Connected to " + activeServer.name, Toast.LENGTH_SHORT).show();
                                        String vDataJson = msg.substring(3);
                                        VolumeData[] recv = JSONManager.deserialize(vDataJson, VolumeData[].class);

                                        MainActivity.Instance.adapter.clear();
                                        MainActivity.Instance.adapter.addAll(recv);


                                        MainActivity.Instance.adapter.notifyDataSetChanged();
                                        for (int i = 0; i < MainActivity.Instance.adapter.listElements.size(); i++) {
                                            VolumeData vm = MainActivity.Instance.adapter.listElements.get(i);
                                            if (vm.title.equals("Master")) //TODO: Use multilanguage title
                                            {
                                                if (i != 0) {
                                                    VolumeData pos0 = MainActivity.Instance.adapter.listElements.get(0);
                                                    MainActivity.Instance.adapter.listElements.set(0, vm);
                                                    MainActivity.Instance.adapter.listElements.set(i, pos0);
                                                }
                                            } else if (vm.title.equals("System"))//TODO: Use multilanguage title
                                            {
                                                if (i != 1) {
                                                    VolumeData pos1 = MainActivity.Instance.adapter.listElements.get(1);
                                                    MainActivity.Instance.adapter.listElements.set(1, vm);
                                                    MainActivity.Instance.adapter.listElements.set(i, pos1);
                                                }
                                            }
                                        }
                                        MainActivity.Instance.adapter.notifyDataSetChanged();
                                    }
                                });

                            } else if (msg.startsWith("ADD")) { //Add a new AudioSession

                                MainActivity.Instance.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String vDataJson = msg.substring(3);

                                        VolumeData recv = JSONManager.deserialize(vDataJson, VolumeData.class);
                                        MainActivity.Instance.adapter.add(recv);
                                        MainActivity.Instance.adapter.refreshProgressDrawables = true;
                                        MainActivity.Instance.adapter.notifyDataSetChanged();
                                    }
                                });
                            } else if (msg.startsWith("EDIT")) // Edit an existing AudioSession
                            {
                                MainActivity.Instance.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String vDataJson = msg.substring(4);


                                        VolumeData received = JSONManager.deserialize(vDataJson, VolumeData.class);

                                        if(received != null) {
                                            for (int i = 0; i < MainActivity.Instance.adapter.listElements.size(); i++) {
                                                if (MainActivity.Instance.adapter.listElements.get(i).id.equals(received.id)) {
                                                    if (!(MainActivity.Instance.adapter.listElements.get(i).mute != received.mute && MainActivity.Instance.adapter.listElements.get(i).ignoreNextMute))
                                                        MainActivity.Instance.adapter.listElements.set(i, received);
                                                    else
                                                        MainActivity.Instance.adapter.listElements.get(i).ignoreNextMute = false;

                                                    break;
                                                }
                                            }

                                            MainActivity.Instance.adapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                            } else if (msg.startsWith("DEL")) // Edit an existing AudioSession
                            {
                                MainActivity.Instance.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String vDataJson = msg.substring(3);

                                        VolumeData received = JSONManager.deserialize(vDataJson, VolumeData.class);

                                        for (int i = 0; i < MainActivity.Instance.adapter.listElements.size(); i++) {

                                            if (MainActivity.Instance.adapter.listElements.get(i).id.equals(received.id)) {
                                                MainActivity.Instance.adapter.listElements.remove(i);
                                                Log.i("ClientThread", "Removing item");
                                                break;
                                            }

                                        }

                                        MainActivity.Instance.adapter.notifyDataSetChanged();
                                    }
                                });
                            } else if (msg.startsWith("IMGS")) // All application icons
                            {
                                String imgDataJson = msg.substring(4);
                                final ApplicationIcon[] icons = JSONManager.deserialize(imgDataJson, ApplicationIcon[].class);
                                Log.i("ClientThread", "Received Icons");
                                MainActivity.Instance.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        for (int i = 0; i < MainActivity.Instance.adapter.listElements.size(); i++) {
                                            for (int j = 0; j < icons.length; j++) {
                                                if (MainActivity.Instance.adapter.listElements.get(i).id.equals(icons[j].id)) {

                                                    byte[] imgBytes = Base64.decode(icons[j].icon, Base64.NO_WRAP);
                                                    Bitmap bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);

                                                    if (bitmap == null) {
                                                        MainActivity.Instance.adapter.sessionIcons.put(MainActivity.Instance.adapter.listElements.get(i).id, BitmapFactory.decodeResource(MainActivity.Instance.getResources(), R.mipmap.application_icon));
                                                    } else {
                                                        MainActivity.Instance.adapter.sessionIcons.put(MainActivity.Instance.adapter.listElements.get(i).id, bitmap);
                                                    }


                                                }
                                            }


                                        }

                                        MainActivity.Instance.adapter.notifyDataSetChanged();
                                    }
                                });

                            }
                            else if(msg.equals("AUTHWPW"))
                            {
                                Log.i(TAG, "Wrong password");

                                SharedPreferences prefs = MainActivity.Instance.getPreferences(MainActivity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(MainActivity.ServerStandardPasswordPrefix_PrefKey+VCCryptography.getMD5Hash(activeServer.RSAPublicKey), "");
                                editor.apply();

                                MainActivity.Instance.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.Instance, "Wrong password for \"" + activeServer.name + "\"", Toast.LENGTH_LONG).show();
                                        activeServer.standardPassword = "";
                                        MainActivity.Instance.serverListViewAdapter.removeActive();
                                    }
                                });
                                return;
                            }
                        }
                        else{
                            endOfStream = true;
                        }

                    } catch (IOException ioe) {
                        if(ioe.getMessage().contains("ECONNRESET")) //Server closed
                        {
                            Log.i(TAG, "Connection to server lost");
                            close();
                            new NetworkDiscoveryThread().start();
                            return;
                        }
                        ioe.printStackTrace();
                    }
                }
                close();
            }
        }
    };

    private enum MessageType{INIT, ADD, REMOVE, ICON}

    public class ClientDevice{
        public String Name;
        public String RSAKeyJSON;
        public int Version;
    }

    public class ApplicationIcon{
        public String icon;
        public String id;
    }





}


package com.nulldozer.volumecontrol;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
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
    
    public String currentMessage;
    
    private ClientConnection clientConnection;
    
    public Thread listenerThread;

    private final String TAG = "ClientThread";

    private MainActivity mainActivity;

    private final static int ReceiverInitTime = 1000;

    public ClientThread(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        clientConnection = mainActivity.clientFragment.clientConnection;
        if(clientConnection == null)
        {
            Log.i(TAG, "Active server null, aborting");
            close();
        }
        else if(!clientConnection.connected)
        {
            Log.i(TAG, "clientConnection not yet connected, connecting...");
            Thread connectThread = new Thread(connectToDataPort);
            connectThread.start();
        }
        else{
            Log.i(TAG, "clientConnection is already connected, fragment retained");
            sendAuthentication();
            listenerThread = new Thread(receiveAudioData);
            listenerThread.start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.sleep(ReceiverInitTime);
                    }
                    catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    requestAllAudioSessionsFromServer.run();
                }
            }).start();
        }
    }

    public void close(){
        Log.w(TAG, "Closing client Thread");

        if(mainActivity != null && mainActivity.serverListViewAdapter != null) {
            if (mainActivity.serverListViewAdapter.activeServer != null)
                mainActivity.serverListViewAdapter.activeServer.active = false;
            mainActivity.serverListViewAdapter.activeServer = null;
            mainActivity.serverListViewAdapter.notifyDataSetChanged();
        }

        if(listenerThread != null)
        listenerThread.interrupt();

        if(mainActivity != null)
        mainActivity.listViewAdapterVolumeSliders.clear();
        mainActivity = null;
    }

    public Runnable connectToDataPort = new Runnable() {
        @Override
        public void run() {
            try {
                Log.i(TAG, "Active Server IP: " + clientConnection.activeServer.IPAddress);

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.networkEventHandlers.onConnectionInitiated(clientConnection.activeServer);
                    }
                });

                clientConnection.connect();
                
                Log.i(TAG, "Connected to Server: " + clientConnection.activeServer.IPAddress);

                ClientDevice thisDevice = new ClientDevice();
                thisDevice.Name = Build.MODEL;
                thisDevice.RSAKeyJSON = VCCryptography.getRSAPublicKeyJSON();
                thisDevice.Version = BuildConfig.VERSION_CODE;

                clientConnection.send("DEVINFO" + JSONManager.serialize(thisDevice));
                sendAuthentication();

                listenerThread = new Thread(receiveAudioData);
                listenerThread.start();

                try{
                    Thread.sleep(ReceiverInitTime);
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
                clientConnection.connected = false;
                close();
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
            if(clientConnection.connected && clientConnection.socket != null && clientConnection.outWriter != null)
            {
                String toSend = JSONManager.serialize(data);
                clientConnection.send("EDIT"+toSend);

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
            if(clientConnection.connected && clientConnection.socket != null && clientConnection.outWriter != null)
            {
                clientConnection.send("TRACK"+data.id);


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
            if(clientConnection.connected && clientConnection.socket != null && clientConnection.outWriter != null)
            {
                clientConnection.send("ENDTRACK"+data.id);

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
            if(clientConnection.connected && clientConnection.socket != null && clientConnection.outWriter != null)
            {
                clientConnection.send("GETAUDIOSESSIONS");

                Log.i("ClientThread", "Requested all audio sessions");
            }
            else{
                Log.i("ClientThread", "Not connected or socket/outWriter is null");
            }
        }
    };

    //So geht encryption
    // byte[] encryptedData = VCCryptography.encrypt((activeServer.hasPassword ? activeServer.getHashedPassword() + ";" : "") + data, activeServer.RSAPublicKey);
    //             if (encryptedData != null) {
    //     byte[] base64Data = Base64.encode(encryptedData, Base64.NO_WRAP);
    //     String encryptedString = new String(base64Data, "UTF-8");
    //     outWriter.println(encryptedString);
    //    outWriter.flush();


    public Runnable receiveAudioData = new Runnable() {
        @Override
        public void run() {
            if(clientConnection.connected && clientConnection.socket != null && clientConnection.outWriter != null && clientConnection.inFromServer != null) {
                Log.i("ClientThread", "Now receiving AudioData...");
                boolean endOfStream = false;
                while (!Thread.interrupted() && !endOfStream) {
                    try {
                        currentMessage = clientConnection.inFromServer.readLine();

                        if(currentMessage == null)
                        {
                            Log.i(TAG, "EOS, Closing ClientThread");
                            close();
                            clientConnection.close();
                        }

                        if (currentMessage != null && !Thread.interrupted()) {
                            final String msg = currentMessage;//VCCryptography.getDecryptedMessage(currentMessage);
                            Log.i("ClientThread", "received: " + msg);

                            if(msg.equals("AUTH")){
                                sendAuthentication();
                            }
                            else if (msg.startsWith("REP")) { //Repopulate Data set (Clear & Set)
                                KnownServerHelper.addToKnown(clientConnection.activeServer.RSAPublicKey);

                                String vDataJson = msg.substring(3);
                                final VolumeData[] recv = JSONManager.deserialize(vDataJson, VolumeData[].class);

                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mainActivity.networkEventHandlers.onAudioSessionListReceived(clientConnection.activeServer, recv);
                                    }
                                });

                            } else if (msg.startsWith("ADD")) { //Add a new AudioSession

                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String vDataJson = msg.substring(3);

                                        VolumeData recv = JSONManager.deserialize(vDataJson, VolumeData.class);
                                        mainActivity.listViewAdapterVolumeSliders.add(recv);
                                        mainActivity.listViewAdapterVolumeSliders.refreshProgressDrawables = true;
                                        mainActivity.listViewAdapterVolumeSliders.notifyDataSetChanged();
                                    }
                                });
                            } else if (msg.startsWith("EDIT")) // Edit an existing AudioSession
                            {
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String vDataJson = msg.substring(4);


                                        VolumeData received = JSONManager.deserialize(vDataJson, VolumeData.class);

                                        if(received != null) {
                                            for (int i = 0; i < mainActivity.listViewAdapterVolumeSliders.listElements.size(); i++) {
                                                if (mainActivity.listViewAdapterVolumeSliders.listElements.get(i).id.equals(received.id)) {
                                                    if (!(mainActivity.listViewAdapterVolumeSliders.listElements.get(i).mute != received.mute && mainActivity.listViewAdapterVolumeSliders.listElements.get(i).ignoreNextMute))
                                                        mainActivity.listViewAdapterVolumeSliders.listElements.set(i, received);
                                                    else
                                                        mainActivity.listViewAdapterVolumeSliders.listElements.get(i).ignoreNextMute = false;

                                                    break;
                                                }
                                            }

                                            mainActivity.listViewAdapterVolumeSliders.notifyDataSetChanged();
                                        }
                                    }
                                });
                            } else if (msg.startsWith("DEL"))
                            {
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String vDataJson = msg.substring(3);

                                        VolumeData received = JSONManager.deserialize(vDataJson, VolumeData.class);

                                        for (int i = 0; i < mainActivity.listViewAdapterVolumeSliders.listElements.size(); i++) {

                                            if (mainActivity.listViewAdapterVolumeSliders.listElements.get(i).id.equals(received.id)) {
                                                mainActivity.listViewAdapterVolumeSliders.listElements.remove(i);
                                                Log.i("ClientThread", "Removing item");
                                                break;
                                            }

                                        }

                                        mainActivity.listViewAdapterVolumeSliders.notifyDataSetChanged();
                                    }
                                });
                            }
                            else if (msg.startsWith("IMGS")) // All application icons
                            {
                                String imgDataJson = msg.substring(4);
                                final ApplicationIcon[] icons = JSONManager.deserialize(imgDataJson, ApplicationIcon[].class);
                                Log.i("ClientThread", "Received Icons");
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0; i < mainActivity.listViewAdapterVolumeSliders.listElements.size(); i++) {
                                            for (int j = 0; j < icons.length; j++) {
                                                if (mainActivity.listViewAdapterVolumeSliders.listElements.get(i).id.equals(icons[j].id)) {

                                                    byte[] imgBytes = Base64.decode(icons[j].icon, Base64.NO_WRAP); //TODO: If more performance needed move this out of run on ui
                                                    Bitmap bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);

                                                    if (bitmap == null) {
                                                        mainActivity.listViewAdapterVolumeSliders.sessionIcons.put(mainActivity.listViewAdapterVolumeSliders.listElements.get(i).id, BitmapFactory.decodeResource(mainActivity.getResources(), R.mipmap.application_icon));
                                                    } else {
                                                        mainActivity.listViewAdapterVolumeSliders.sessionIcons.put(mainActivity.listViewAdapterVolumeSliders.listElements.get(i).id, bitmap);
                                                    }


                                                }
                                            }
                                        }
                                        mainActivity.listViewAdapterVolumeSliders.notifyDataSetChanged();
                                    }
                                });

                            }
                            else if (msg.startsWith("IMG")) // Single application Icon
                            {
                                String imgDataJson = msg.substring(3);
                                final ApplicationIcon icon = JSONManager.deserialize(imgDataJson, ApplicationIcon.class);
                                byte[] imgBytes = Base64.decode(icon.icon, Base64.NO_WRAP);
                                final Bitmap bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);

                                Log.i("ClientThread", "Received Icon");
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        for (int i = 0; i < mainActivity.listViewAdapterVolumeSliders.listElements.size(); i++) {
                                            if (mainActivity.listViewAdapterVolumeSliders.listElements.get(i).id.equals(icon.id)) {
                                                if (bitmap == null) {
                                                    mainActivity.listViewAdapterVolumeSliders.sessionIcons.put(mainActivity.listViewAdapterVolumeSliders.listElements.get(i).id, BitmapFactory.decodeResource(mainActivity.getResources(), R.mipmap.application_icon));
                                                } else {
                                                    mainActivity.listViewAdapterVolumeSliders.sessionIcons.put(mainActivity.listViewAdapterVolumeSliders.listElements.get(i).id, bitmap);
                                                }
                                            }
                                        }
                                        mainActivity.listViewAdapterVolumeSliders.notifyDataSetChanged();
                                    }
                                });

                            }
                            else if(msg.equals("AUTHWPW"))
                            {
                                Log.i(TAG, "Wrong password");

                                SharedPreferences prefs = mainActivity.getPreferences(MainActivity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(PrefKeys.ServerStandardPasswordPrefix +VCCryptography.getMD5Hash(clientConnection.activeServer.RSAPublicKey), "");
                                editor.apply();

                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mainActivity, "Wrong password for \"" + clientConnection.activeServer.name + "\"", Toast.LENGTH_LONG).show();
                                        clientConnection.activeServer.standardPassword = "";
                                        mainActivity.serverListViewAdapter.removeActive();
                                    }
                                });
                                return;
                            }
                        }
                        else{
                            endOfStream = true;
                        }

                    } catch (IOException ioe) {
                            Log.i(TAG, "Connection to server lost");
                            ioe.printStackTrace();
                            close();

                            mainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mainActivity.networkEventHandlers.onServerDisconnected();
                                }
                            });

                            return;
                    }
                }
                Log.i(TAG, "Stopped receiving data " + (endOfStream ? " (End of Stream)" : " (Thread Interrupted)"));
            }
        }
    };

    public void sendAuthentication(){
        try {
            byte[] encryptedData = VCCryptography.encrypt(VCCryptography.getMD5Hash(clientConnection.activeServer.standardPassword), clientConnection.activeServer.RSAPublicKey);
            byte[] base64Data = Base64.encode(encryptedData, Base64.NO_WRAP);
            String encryptedString = new String(base64Data, "UTF-8");
            String authMessage = "AUTH" + encryptedString;
            clientConnection.send(authMessage);
        }
        catch (UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
        }
    }

}


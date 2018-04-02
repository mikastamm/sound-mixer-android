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

    public boolean connected;

    public String currentMessage;

    private Socket socket;
    private BufferedReader inFromServer;
    private OutputStream outstream;
    private PrintWriter outWriter;

    VolumeServer activeServer;

    public Thread listenerThread;

    private final String TAG = "ClientThread";

    MainActivity mainActivity;
    
    public ClientThread(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        if(mainActivity.serverListViewAdapter.activeServer != null) {
            activeServer = mainActivity.serverListViewAdapter.activeServer;
            Thread connectThread = new Thread(connectToDataPort);
            connectThread.start();
        }
        else{
            Log.i(TAG, "Active server null, aborting");
            close();
        }
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
            mainActivity.serverListViewAdapter.activeServer = null;
            mainActivity.listViewAdapterVolumeSliders.clear();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        if(listenerThread != null)
        listenerThread.interrupt();


    }


    public void send(String data)
    {
            if(outWriter != null) {
                outWriter.println(data);
                outWriter.flush();
            }
    }

    public Runnable connectToDataPort = new Runnable() {
        @Override
        public void run() {
            try {
                Log.i(TAG, "Active Server IP: " + activeServer.IPAddress);

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.networkEventHandlers.onConnectionInitiated(activeServer);
                    }
                });

                socket = new Socket(InetAddress.getByName(activeServer.IPAddress), Constants.SERVER_CONNECTION_TCP_PORT);
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

                sendAuthentication();

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
            if(connected && socket != null && outWriter != null && inFromServer != null) {
                Log.i("ClientThread", "Now receiving AudioData...");
                boolean endOfStream = false;
                while (!Thread.interrupted() && !endOfStream) {
                    try {
                        currentMessage = inFromServer.readLine();

                        if(mainActivity != null)
                        if (currentMessage != null && !Thread.interrupted()) {
                            final String msg = currentMessage;//VCCryptography.getDecryptedMessage(currentMessage);
                            Log.i("ClientThread", "received: " + msg);

                            if(msg.equals("AUTH")){
                                sendAuthentication();
                            }
                            else if (msg.startsWith("REP")) { //Repopulate Data set (Clear & Set)
                                handleRepopulateReceived(msg);
                            } else if (msg.startsWith("ADD")) { //Add a new AudioSession
                                handleAddNewSessionReceived(msg);
                            } else if (msg.startsWith("EDIT")) // Edit an existing AudioSession
                            {
                                handleEditSessionReceived(msg);
                            } else if (msg.startsWith("DEL")) // Delete an existing AudioSession
                            {
                                handleRemoveApplicationReceived(msg);
                            }
                            else if (msg.startsWith("IMGS")) // All application icons
                            {
                               handleImagesReceived(msg);
                            }
                            else if (msg.startsWith("IMG")) // Single application Icon
                            {
                                handleSingleImageReceived(msg);
                            }
                            else if(msg.equals("AUTHWPW"))
                            {
                                handleWrongPasswordReceived();
                                return;
                            }
                        }
                        else{
                            endOfStream = true;
                        }

                    } catch (IOException ioe) {
                            Log.i(TAG, "Connection to server lost");
                            ioe.printStackTrace();

                            if(mainActivity != null)
                            mainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mainActivity.networkEventHandlers.onServerDisconnected();
                                }
                            });
                            close();
                            return;
                    }
                }
                if(mainActivity != null)
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainActivity.networkEventHandlers.onServerDisconnected();
                        }
                    });
                close();
            }
        }
    };

    private void handleRepopulateReceived(String msg){
        PrefHelper.setStringPreference(mainActivity, PrefKeys.LastConnectedServer, activeServer.RSAPublicKey);

        String vDataJson = msg.substring(3);
        final VolumeData[] recv = JSONManager.deserialize(vDataJson, VolumeData[].class);

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.networkEventHandlers.onAudioSessionListReceived(activeServer, recv);
            }
        });
    }

    private void handleAddNewSessionReceived(final String msg)
    {
        String vDataJson = msg.substring(3);
        final VolumeData recv = JSONManager.deserialize(vDataJson, VolumeData.class);

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.listViewAdapterVolumeSliders.add(recv);
                mainActivity.listViewAdapterVolumeSliders.refreshProgressDrawables = true;
                mainActivity.listViewAdapterVolumeSliders.notifyDataSetChanged();
            }
        });
    }

    private void handleEditSessionReceived(final String msg){

        String vDataJson = msg.substring(4);
        final VolumeData received = JSONManager.deserialize(vDataJson, VolumeData.class);

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
    }

    private void handleRemoveApplicationReceived(final String msg)
    {
        String vDataJson = msg.substring(3);
        final VolumeData received = JSONManager.deserialize(vDataJson, VolumeData.class);

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {


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

    private void handleImagesReceived(String msg){
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

    private void handleSingleImageReceived(String msg){
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

    private void handleWrongPasswordReceived(){
        Log.i(TAG, "Wrong password");
        PrefHelper.setStringPreference(mainActivity, PrefKeys.ServerStandardPasswordPrefix+VCCryptography.getMD5Hash(activeServer.RSAPublicKey), "");
        activeServer.standardPassword = "";

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mainActivity, "Wrong password for \"" + activeServer.name + "\"", Toast.LENGTH_LONG).show();
                mainActivity.serverListViewAdapter.removeActive();
            }
        });
    }

    public void sendAuthentication(){
        try {
            byte[] encryptedData = VCCryptography.encrypt(VCCryptography.getMD5Hash(activeServer.standardPassword), activeServer.RSAPublicKey);
            byte[] base64Data = Base64.encode(encryptedData, Base64.NO_WRAP);
            String encryptedString = new String(base64Data, "UTF-8");
            String authMessage = "AUTH" + encryptedString;
            send(authMessage);
        }
        catch (UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
        }
    }

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


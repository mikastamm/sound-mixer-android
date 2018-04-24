package mikastamm.com.soundmixer.Networking.MessageSenders;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.ServerConnection;

/**
 * Created by Mika on 03.04.2018.
 */

public class TrackStartMessageSender extends MessageSender {
    public static String messageTag = "TRACK";

    private ServerConnection connection;
    private String trackedSessionId;

    private Runnable sendTrackStartRunnable = new Runnable() {
        @Override
        public void run() {
            if(connection != null && connection.isConnected())
            {
                connection.writeLine(messageTag + trackedSessionId);
                Log.i(MainActivity.TAG, "Started tracking " + trackedSessionId);
            }
            else
                Log.i(MainActivity.TAG, "Server Connection Object is null or not connected");

            startNextMessageSender();
        }
    };

    public TrackStartMessageSender(String trackedSessionId, ServerConnection connection)
    {
        this.trackedSessionId = trackedSessionId;
        this.connection = connection;
    }

    @Override
    public void send(){
        new Thread(sendTrackStartRunnable).start();
    }

}

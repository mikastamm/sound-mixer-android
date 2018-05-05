package mikastamm.com.soundmixer.Networking.MessageSenders;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.ServerConnection;

/**
 * Created by Mika on 03.04.2018.
 */

public class TrackEndMessageSenderRunnable implements Runnable {
    public static String messageTag = "ENDTRACK{";

    private ServerConnection connection;
    private String trackedSessionId;

    public TrackEndMessageSenderRunnable(String trackedSessionId, ServerConnection connection) {
        this.trackedSessionId = trackedSessionId;
        this.connection = connection;
    }


    @Override
    public void run() {
        if (connection != null && connection.isConnected()) {
            connection.writeLine(messageTag + trackedSessionId);
            Log.i(MainActivity.TAG, "Stopped tracking " + trackedSessionId);
        } else
            Log.i(MainActivity.TAG, "Server Connection Object is null or not connected");
    }


}

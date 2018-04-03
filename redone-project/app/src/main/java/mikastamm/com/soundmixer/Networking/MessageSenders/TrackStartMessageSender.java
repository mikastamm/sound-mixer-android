package mikastamm.com.soundmixer.Networking.MessageSenders;

import android.util.Log;

import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Helpers.Json;
import mikastamm.com.soundmixer.Networking.ServerConnection;

/**
 * Created by Mika on 03.04.2018.
 */

public class TrackStartMessageSender implements MessageSender {
    public static String messageTag = "TRACK";

    private ServerConnection connection;
    private String trackedSessionId;

    private Runnable sendTrackStartRunnable = new Runnable() {
        @Override
        public void run() {
            if(connection != null && connection.isConnected())
            {
                connection.writeLine(messageTag + trackedSessionId);
                Log.i(this.getClass().getSimpleName(), "Started tracking " + trackedSessionId);
            }
            else
                Log.i(this.getClass().getSimpleName(), "Server Connection Object is null or not connected");
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

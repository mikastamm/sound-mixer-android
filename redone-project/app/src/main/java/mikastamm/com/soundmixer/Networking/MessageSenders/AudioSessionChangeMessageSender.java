package mikastamm.com.soundmixer.Networking.MessageSenders;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Helpers.Json;
import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.ServerConnection;

/**
 * Created by Mika on 03.04.2018.
 */

public class AudioSessionChangeMessageSender extends MessageSender{
    public static String messageTag = "EDIT";

    private ServerConnection connection;
    private AudioSession changedSession;

    private Runnable sendChangeRunnable = new Runnable() {
        @Override
        public void run() {
            if(connection != null && connection.isConnected())
            {
                connection.writeLine(messageTag + Json.serialize(changedSession));
                Log.i(MainActivity.TAG, "Sent AudioSession Change of " + changedSession.title);
            }
            else
                Log.i(MainActivity.TAG, "Server Connection Object is null or not connected");
            startNextMessageSender();
        }
    };

    public AudioSessionChangeMessageSender(AudioSession changedSession, ServerConnection connection)
    {
        this.changedSession = changedSession.copy();
        this.changedSession.icon = null;
        this.connection = connection;
    }

    @Override
    public void send(){
        new Thread(sendChangeRunnable).start();
    }

}

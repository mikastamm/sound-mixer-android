package mikastamm.com.soundmixer.Networking.MessageHandlers;

import android.util.Log;

import mikastamm.com.soundmixer.ClientAudioSessions;
import mikastamm.com.soundmixer.ClientAudioSessionsManager;
import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Helpers.Json;
import mikastamm.com.soundmixer.MainActivity;

/**
 * Created by Mika on 01.04.2018.
 */

public class RepopulateAudioSessionsMessageHandler implements ReceivedMessageHandler {
    public static String messageTypeIdentifierPrefix = "EDIT";

    private String message;
    private String serverId;

    public RepopulateAudioSessionsMessageHandler(String message, String serverId)
    {
        this.message = message;
        this.serverId = serverId;
    }

    @Override
    public void handleMessage() {
        //Deserialize received Json
        String vDataJson = message.substring(3);
        AudioSession[] recv = Json.deserialize(vDataJson, AudioSession[].class);

        //Clear and then add the AudioSession to the datamodel
        ClientAudioSessions sessions = ClientAudioSessionsManager.getClientAudioSessions(serverId);
        sessions.clearAudioSessions();
        sessions.addAudioSessions(recv);
        Log.i(MainActivity.TAG, "REP Received: " + recv.length + " Audio Sessions");
    }
}

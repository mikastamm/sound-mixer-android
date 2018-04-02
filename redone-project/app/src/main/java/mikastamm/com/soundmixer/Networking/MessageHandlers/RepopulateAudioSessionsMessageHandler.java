package mikastamm.com.soundmixer.Networking.MessageHandlers;

import mikastamm.com.soundmixer.ClientAudioSessions;
import mikastamm.com.soundmixer.ClientAudioSessionsManager;
import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.Helpers.Json;

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
        String vDataJson = message.substring(3);
        AudioSession[] recv = Json.deserialize(vDataJson, AudioSession[].class);

        ClientAudioSessions sessions = ClientAudioSessionsManager.getClientAudioSession(serverId);
        sessions.clearAudioSessions();
        sessions.addAudioSessions(recv);
    }
}

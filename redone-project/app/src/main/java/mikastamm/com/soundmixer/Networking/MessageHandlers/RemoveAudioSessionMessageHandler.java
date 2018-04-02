package mikastamm.com.soundmixer.Networking.MessageHandlers;

import mikastamm.com.soundmixer.ClientAudioSessions;
import mikastamm.com.soundmixer.ClientAudioSessionsManager;
import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.Helpers.Json;

/**
 * Created by Mika on 29.03.2018.
 */

public class RemoveAudioSessionMessageHandler implements ReceivedMessageHandler{
    public static String messageTypeIdentifierPrefix = "DEL";

    private String message;
    private String serverId;

    public RemoveAudioSessionMessageHandler(String message, String serverId)
    {
        this.message = message;
        this.serverId = serverId;
    }

    @Override
    public void handleMessage() {
        String jsonData = message.substring(messageTypeIdentifierPrefix.length());
        AudioSession newSession = Json.deserialize(jsonData, AudioSession.class);

        ClientAudioSessions sessions = ClientAudioSessionsManager.getClientAudioSession(serverId);
        sessions.removeAudioSession(newSession.id);
    }
}
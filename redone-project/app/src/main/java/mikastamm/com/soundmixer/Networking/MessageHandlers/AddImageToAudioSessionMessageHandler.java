package mikastamm.com.soundmixer.Networking.MessageHandlers;

import mikastamm.com.soundmixer.ClientAudioSessions;
import mikastamm.com.soundmixer.ClientAudioSessionsManager;
import mikastamm.com.soundmixer.Datamodel.AudioSessionIcon;
import mikastamm.com.soundmixer.Helpers.Json;

/**
 * Created by Mika on 02.04.2018.
 */

public class AddImageToAudioSessionMessageHandler implements ReceivedMessageHandler {
    public static String messageTypeIdentifierPrefix = "IMG";

    private String message;
    private String serverId;

    public AddImageToAudioSessionMessageHandler(String message, String serverId) {
        this.message = message;
        this.serverId = serverId;
    }

    @Override
    public void handleMessage() {
        //Deserialize received Json
        String jsonData = message.substring(messageTypeIdentifierPrefix.length());
        AudioSessionIcon icon = Json.deserialize(jsonData, AudioSessionIcon.class);

        //Add image to datamodel
        ClientAudioSessions sessions = ClientAudioSessionsManager.getClientAudioSessions(serverId);
        sessions.setAudioSessionImage(icon);
    }


}

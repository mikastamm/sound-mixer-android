package mikastamm.com.soundmixer.Networking.MessageHandlers;

import mikastamm.com.soundmixer.ClientAudioSessions;
import mikastamm.com.soundmixer.ClientAudioSessionsManager;
import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Datamodel.AudioSessionIcon;
import mikastamm.com.soundmixer.Helpers.Json;

/**
 * Created by Mika on 02.04.2018.
 */

public class AddImageToAudioSessionMessageHandler implements ReceivedMessageHandler{
    public static String messageTypeIdentifierPrefix;

    private String message;
    private String serverId;
    private boolean multiple;

    public AddImageToAudioSessionMessageHandler(String message, String serverId, boolean multiple)
    {
        this.message = message;
        this.serverId = serverId;
        this.multiple = multiple;

        if(multiple)
            messageTypeIdentifierPrefix = "IMGS";
        else
            messageTypeIdentifierPrefix = "IMG";
    }

    @Override
    public void handleMessage() {
        if(multiple) {
            String jsonData = message.substring(messageTypeIdentifierPrefix.length());
            AudioSessionIcon[] icons = Json.deserialize(jsonData, AudioSessionIcon[].class);

            ClientAudioSessions sessions = ClientAudioSessionsManager.getClientAudioSession(serverId);
            for(AudioSessionIcon icon : icons) {
                sessions.setAudioSessionImage(icon);
            }
        }
        else{
            String jsonData = message.substring(messageTypeIdentifierPrefix.length());
            AudioSessionIcon icon = Json.deserialize(jsonData, AudioSessionIcon.class);

            ClientAudioSessions sessions = ClientAudioSessionsManager.getClientAudioSession(serverId);
            sessions.setAudioSessionImage(icon);
        }
    }


}

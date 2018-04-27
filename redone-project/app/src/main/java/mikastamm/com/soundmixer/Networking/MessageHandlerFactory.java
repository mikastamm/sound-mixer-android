package mikastamm.com.soundmixer.Networking;

import mikastamm.com.soundmixer.Networking.MessageHandlers.AddAudioSessionMessageHandler;
import mikastamm.com.soundmixer.Networking.MessageHandlers.AddImageToAudioSessionMessageHandler;
import mikastamm.com.soundmixer.Networking.MessageHandlers.EditAudioSessionMessageHandler;
import mikastamm.com.soundmixer.Networking.MessageHandlers.ReceivedMessageHandler;
import mikastamm.com.soundmixer.Networking.MessageHandlers.RemoveAudioSessionMessageHandler;
import mikastamm.com.soundmixer.Networking.MessageHandlers.RepopulateAudioSessionsMessageHandler;

/**
 * Created by Mika on 28.03.2018.
 */

public class MessageHandlerFactory {
    public static ReceivedMessageHandler getHandler(String msg, String serverId)
    {
        if(msg.equals("AUTH"))
        {

        }
        else if (msg.startsWith("REP")) //Repopulate Data set (Clear & Set)
        {
            return new RepopulateAudioSessionsMessageHandler(msg, serverId);
        }
        else if (msg.startsWith(AddAudioSessionMessageHandler.messageTypeIdentifierPrefix)) //Add a new AudioSession
        {
            return new AddAudioSessionMessageHandler(msg, serverId);
        }
        else if (msg.startsWith(EditAudioSessionMessageHandler.messageTypeIdentifierPrefix)) // Edit an existing AudioSession
        {
            return new EditAudioSessionMessageHandler(msg, serverId);
        }
        else if (msg.startsWith("DEL")) // Delete an existing AudioSession
        {
            return new RemoveAudioSessionMessageHandler(msg, serverId);
        }
        else if (msg.startsWith("IMGS")) // All application icons
        {
            return new AddImageToAudioSessionMessageHandler(msg, serverId);
        }
        else if (msg.startsWith("IMG")) // Single application Icon
        {
            return new AddImageToAudioSessionMessageHandler(msg, serverId);
        }
        else if(msg.equals("AUTHWPW"))
        {
        }

        return null;
    }
}

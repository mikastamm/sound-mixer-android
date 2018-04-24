package mikastamm.com.soundmixer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mika on 28.03.2018.
 */

public class ClientAudioSessionsManager {
    private static Map<String, ClientAudioSessions> clientAudioSessions = new HashMap<>();
    public static ClientAudioSessions getClientAudioSessions(String serverId)
    {
        return clientAudioSessions.get(serverId);
    }

    public static ClientAudioSessions registerClientAudioSessions(String serverId)
    {
        if (clientAudioSessions.get(serverId) == null)
            clientAudioSessions.put(serverId, new ClientAudioSessions());

        return clientAudioSessions.get(serverId);
    }
}

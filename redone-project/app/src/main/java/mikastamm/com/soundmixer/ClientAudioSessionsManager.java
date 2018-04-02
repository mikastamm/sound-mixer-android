package mikastamm.com.soundmixer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mika on 28.03.2018.
 */

public class ClientAudioSessionsManager {
    private static Map<String, ClientAudioSessions> clientAudioSessions = new HashMap<>();
    public static ClientAudioSessions getClientAudioSession(String serverId)
    {
        return clientAudioSessions.get(serverId);
    }

    public static ClientAudioSessions registerClientAudioSession(String serverId)
    {
        clientAudioSessions.put(serverId, new ClientAudioSessions());
        return clientAudioSessions.get(serverId);
    }
}

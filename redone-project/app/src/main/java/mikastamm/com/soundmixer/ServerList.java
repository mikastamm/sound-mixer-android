package mikastamm.com.soundmixer;

import java.util.HashMap;
import java.util.Map;

import mikastamm.com.soundmixer.Datamodel.Server;

/**
 * Created by Mika on 27.03.2018.
 */

public class ServerList{

    //Instantiation (Singleton)
    private ServerList(){}
    private static ServerList instance = null;
    public static ServerList getInstance()
    {
        if(instance == null)
            instance = new ServerList();
        return instance;
    }

    //Logic
    public ServerListeners listeners = new ServerListeners();
    private Map<String, Server> servers = new HashMap<>();
    private Server active;

    public void addServer(Server server)
    {
        servers.put(server.id, server);
        listeners.onServerDiscovered(server);
    }

    public void removeServer(Server server)
    {
        servers.remove(server.id);
        listeners.onServerLost(server);
    }

    public Server getServer(String rsaKey)
    {
        return servers.get(rsaKey);
    }

    public void setActiveServer(Server newActive)
    {
        final Server oldActive = active;
        if(active != null)
            active.state = ServerState.connected;
        active = newActive;
        active.state = ServerState.active;

        listeners.onActiveServerChanged(oldActive, newActive);
    }

    public int size(){
        return servers.size();
    }
}

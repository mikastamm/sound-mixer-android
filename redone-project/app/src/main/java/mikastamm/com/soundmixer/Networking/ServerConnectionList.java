package mikastamm.com.soundmixer.Networking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.ServerList;

/**
 * Created by Mika on 11.04.2018.
 */

public class ServerConnectionList {
    //Instantiation (Singleton)
    private ServerConnectionList(){}
    private static ServerConnectionList instance = null;

    public static ServerConnectionList getInstance()
    {
        if(instance == null)
            instance = new ServerConnectionList();
        return instance;
    }

    //Logic
    private Set<ServerConnection> connections = new HashSet<>();
    public void add(ServerConnection connection)
    {
        connections.add(connection);
    }

    public ServerConnection get(String serverId)
    {
        ServerConnection ret = null;
        for(ServerConnection c : connections)
        {
            if(c.getServer().id.equals(serverId))
                ret = c;
        }
        return ret;
    }

    public boolean contains(String serverId)
    {
        boolean ret = false;
        for(ServerConnection c : connections)
        {
            if(c.getServer().id.equals(serverId))
                ret = true;
        }
        return ret;
    }

    public void disconnectAndRemove(String serverId)
    {
        ServerConnection toDelete = null;
        for(ServerConnection c : connections)
        {
            if(c.getServer().id.equals(serverId))
            {
                toDelete = c;
            }
        }

        if(toDelete != null) {
            toDelete.dispose();
            connections.remove(toDelete);
        }
    }

    public void disconnectAndClear(){
        for(ServerConnection c : connections)
        {
            c.dispose();
        }

        connections.clear();
    }
}

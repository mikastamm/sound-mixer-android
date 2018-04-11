package mikastamm.com.soundmixer;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.Exceptions.InvalidServerException;

/**
 * Created by Mika on 27.03.2018.
 */

public class ServerList implements Iterable<Server>{

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
    private final Map<String, Server> servers = new HashMap<>();
    private Server active;

    public void addServer(Server server)
    {
        try {
            validateServer(server);
            synchronized (servers) {
                servers.put(server.id, server);
            }
            listeners.onServerDiscovered(server);
        }
        catch (InvalidServerException ex)
        {
            ex.printStackTrace();
        }
    }

    public void removeServer(Server server)
    {
        try {
            validateServer(server);
            synchronized (servers) {
                servers.remove(server.id);
            }
            listeners.onServerLost(server);
        }
         catch (InvalidServerException ex)
        {
            ex.printStackTrace();
        }
    }

    public Server getServer(String rsaKey)
    {
        return servers.get(rsaKey);
    }

    public void setActiveServer(Server newActive)
    {
        try {
            validateServer(newActive);

            final Server oldActive = active;
            if (active != null)
                active.state = ServerState.connected;
            active = newActive;
            active.state = ServerState.active;

            listeners.onActiveServerChanged(oldActive, newActive);

        }
        catch (InvalidServerException ex)
        {
            ex.printStackTrace();
        }
    }

    private void validateServer(Server server) throws InvalidServerException
    {
        if(server.id == null)
            throw new InvalidServerException("Id is null");

        if(server.name == null)
            throw new InvalidServerException("Name is null");
    }

    public int size(){
        return servers.size();
    }

    @NonNull
    @Override
    public Iterator<Server> iterator() {
        return new ServerIterator(new ArrayList<Server>(servers.values()));
    }

    private static final class ServerIterator implements Iterator<Server> {
        private List<Server> servers;
        private int cursor = 0;
        private int end;

        public ServerIterator(List<Server> servers) {
            this.servers = servers;
            end = servers.size();
        }

        public boolean hasNext() {
            return this.cursor < end;
        }

        public Server next() {
            if(this.hasNext()) {
                return servers.get(cursor++);
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

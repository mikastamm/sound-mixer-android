package mikastamm.com.soundmixer;

import android.support.annotation.NonNull;

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
            listeners.serverDiscovered(server);
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
            listeners.serverLost(server);
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

    public Server getActiveServer() {
        return active;
    }

    public void setActiveServer(Server newActive)
    {
        try {
            validateServer(newActive);

            Server oldActive = active;

            if(oldActive != null && oldActive.id.equals(newActive.id))
                oldActive = null;

            if (oldActive != null)
                oldActive.state = ServerState.connected;

            active = newActive;
            active.state = ServerState.active;

            listeners.activeServerChanged(oldActive, newActive);

        }
        catch (InvalidServerException ex)
        {
            ex.printStackTrace();
        }
    }

    public void removeActiveServer(){
        if(active != null) {
            active.state = ServerState.connected;
            Server oldActive = active;
            active = null;
            listeners.activeServerChanged(oldActive, null);
        }
    }

    private void validateServer(Server server) throws InvalidServerException
    {
        if(server == null)
            throw new InvalidServerException("Server is null");

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

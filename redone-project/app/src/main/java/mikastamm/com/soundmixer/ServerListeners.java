package mikastamm.com.soundmixer;

import java.util.ArrayList;
import java.util.List;

import mikastamm.com.soundmixer.Datamodel.Server;

/**
 * Created by Mika on 28.03.2018.
 */

public class ServerListeners {
    public interface ServerListChangeListener{
        void onServerDiscovered(Server server);
        void onServerLost(Server server);
    }

    private final List<ServerListChangeListener> serverListChangeListeners = new ArrayList<>();
    public void addServerListChangeListener(ServerListChangeListener listener)
    {
        synchronized (serverListChangeListeners) {
            serverListChangeListeners.add(listener);
        }
    }

    public void removeServerListChangeListener(ServerListChangeListener listener)
    {
        synchronized (serverListChangeListeners) {
            serverStateChangeListeners.remove(listener);
        }
    }

    public void serverDiscovered(Server server){
        synchronized (serverListChangeListeners) {
            for (ServerListChangeListener listener : serverListChangeListeners) {
                listener.onServerDiscovered(server);
            }
        }
    }

    public void serverLost(Server server){
        synchronized (serverListChangeListeners) {
            for (ServerListChangeListener listener : serverListChangeListeners) {
                listener.onServerLost(server);
            }
        }
    }

    public interface ServerStateChangeListener {
        void onServerConnected(Server server);
        void onServerDisconnected(Server server);
        void onActiveServerChanged(Server oldActive, Server newActive);
    }

    private final List<ServerStateChangeListener> serverStateChangeListeners = new ArrayList<>();
    public void addServerStateChangeListener(ServerStateChangeListener listener)
    {
        synchronized (serverStateChangeListeners) {
            serverStateChangeListeners.add(listener);
        }
    }

    public void removeServerChangeListener(ServerStateChangeListener listener)
    {
        synchronized (serverStateChangeListeners) {
            serverStateChangeListeners.remove(listener);
        }
    }

    public void serverConnected(Server server)
    {
        server.state = ServerState.connected;
        synchronized (serverStateChangeListeners) {
            for (ServerStateChangeListener listener : serverStateChangeListeners) {
                listener.onServerConnected(server);
            }
        }
    }

    public void serverDisconnected(Server server)
    {
        server.state = ServerState.available;
        synchronized (serverStateChangeListeners) {
            for (ServerStateChangeListener listener : serverStateChangeListeners) {
                listener.onServerDisconnected(server);
            }
        }
    }

    public void activeServerChanged(Server oldActive, Server newActive)
    {
        if(newActive != null)
        newActive.state = ServerState.active;

        synchronized (serverStateChangeListeners) {
            for (ServerStateChangeListener listener : serverStateChangeListeners) {
                listener.onActiveServerChanged(oldActive, newActive);
            }
        }
    }
}

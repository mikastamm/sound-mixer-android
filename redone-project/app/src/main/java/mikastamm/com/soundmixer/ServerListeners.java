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

    public List<ServerListChangeListener> serverListChangeListeners = new ArrayList<>();
    public void addServerListChangeListener(ServerListChangeListener listener)
    {
        serverListChangeListeners.add(listener);
    }

    public void removeServerListChangeListener(ServerListChangeListener listener)
    {
        serverStateChangeListeners.remove(listener);
    }

    public void onServerDiscovered(Server server){
        for (ServerListChangeListener listener : serverListChangeListeners) {
            listener.onServerDiscovered(server);
        }
    }

    public void onServerLost(Server server){
        for (ServerListChangeListener listener : serverListChangeListeners) {
            listener.onServerLost(server);
        }
    }

    public interface ServerStateChangeListener {
        void onServerConnected(Server server);
        void onServerDisconnected(Server server);
        void onActiveServerChanged(Server oldActive, Server newActive);
    }

    public List<ServerStateChangeListener> serverStateChangeListeners = new ArrayList<>();
    public void addServerStateChangeListener(ServerStateChangeListener listener)
    {
        serverStateChangeListeners.add(listener);
    }

    public void removeServerChangeListener(ServerStateChangeListener listener)
    {
        serverStateChangeListeners.remove(listener);
    }

    public void onServerConnected(Server server)
    {
        for (ServerStateChangeListener listener : serverStateChangeListeners) {
            listener.onServerConnected(server);
        }
    }

    public void onServerDisconnected(Server server)
    {
        for (ServerStateChangeListener listener : serverStateChangeListeners) {
            listener.onServerDisconnected(server);
        }
    }

    public void onActiveServerChanged(Server oldActive, Server newActive)
    {
        for (ServerStateChangeListener listener : serverStateChangeListeners) {
            listener.onActiveServerChanged(oldActive, newActive);
        }
    }
}

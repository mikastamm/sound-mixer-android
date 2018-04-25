package mikastamm.com.soundmixer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.Helpers.AutoResetEvent;

/**
 * Created by Mika on 28.03.2018.
 */

public class ServerListeners {
    public interface ServerListChangeListener {
        void onServerDiscovered(Server server);

        void onServerLost(Server server);
    }

    private final List<ServerListChangeListener> serverListChangeListeners = new ArrayList<>();

    public void addServerListChangeListener(ServerListChangeListener listener) {
        serverListChangeListeners.add(listener);
    }

    public void removeServerListChangeListener(ServerListChangeListener listener) {
        serverListChangeListeners.remove(listener);
    }

    public void serverDiscovered(Server server) {
        List<ServerListChangeListener> serverListChangeListenersCopy = new ArrayList<>(serverListChangeListeners);
        for (ServerListChangeListener listener : serverListChangeListenersCopy) {
            listener.onServerDiscovered(server);
        }
    }

    public void serverLost(Server server) {
        List<ServerListChangeListener> serverListChangeListenersCopy = new ArrayList<>(serverListChangeListeners);
        for (ServerListChangeListener listener : serverListChangeListenersCopy) {
            listener.onServerLost(server);
        }
    }

    public interface ServerStateChangeListener {
        void onServerConnected(Server server);

        void onServerDisconnected(Server server);

        void onActiveServerChanged(Server oldActive, Server newActive);
    }

    private final List<ServerStateChangeListener> serverStateChangeListeners = new ArrayList<>();

    public void addServerStateChangeListener(ServerStateChangeListener listener) {
        serverStateChangeListeners.add(listener);
    }

    public void removeServerStateChangeListener(ServerStateChangeListener listener) {
        serverStateChangeListeners.remove(listener);
    }

    public void serverConnected(Server server) {
        server.state = ServerState.connected;

        List<ServerStateChangeListener> serverStateChangeListenersCopy = new ArrayList<>(serverStateChangeListeners);
        for (ServerStateChangeListener listener : serverStateChangeListenersCopy) {
            listener.onServerConnected(server);
        }
    }

    public void serverDisconnected(Server server) {
        server.state = ServerState.available;

        List<ServerStateChangeListener> serverStateChangeListenersCopy = new ArrayList<>(serverStateChangeListeners);
        for (ServerStateChangeListener listener : serverStateChangeListenersCopy) {
            listener.onServerDisconnected(server);
        }
    }

    public void activeServerChanged(Server oldActive, Server newActive) {
        if (newActive != null)
            newActive.state = ServerState.active;

        List<ServerStateChangeListener> serverStateChangeListenersCopy = new ArrayList<>(serverStateChangeListeners);
        for (ServerStateChangeListener listener : serverStateChangeListenersCopy) {
            listener.onActiveServerChanged(oldActive, newActive);
        }
    }
}

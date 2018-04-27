package mikastamm.com.soundmixer;

import java.util.ArrayList;
import java.util.List;

import mikastamm.com.soundmixer.Datamodel.Server;

/**
 * Created by Mika on 27.04.2018.
 */

public class ServerListChangeDelegate {
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
}

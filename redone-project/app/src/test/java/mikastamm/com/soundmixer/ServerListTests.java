package mikastamm.com.soundmixer;

import junit.framework.Assert;

import org.junit.Test;

import java.util.List;

import mikastamm.com.soundmixer.Datamodel.Server;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by Mika on 27.03.2018.
 */

public class ServerListTests {
    public ServerList list = ServerList.getInstance();

    int serverCount = 10;
    String[] rsaKeys;
    public void fillServerList(){
        rsaKeys = MockDataFactory.getRSAKeys(serverCount);
        for(int i = 0; i < serverCount; i++)
        {
            list.addServer(new Server(false, "Name", "10.0.0.1", rsaKeys[i]));
        }
    }

    public void emptyServerList(){
        for(int i = serverCount-1; i >= 0 ; i--)
        {
            list.removeServer(list.getServer(rsaKeys[i]));
        }
    }

    private int connected =0;
    private int disconnected = 0;
    private int activeServerChanged = 0;

    @Test
    public void test_ServerChangeListenerFires(){

        list.listeners.addServerStateChangeListener(new ServerListeners.ServerStateChangeListener() {
            @Override
            public void onServerConnected(Server server) {
                connected++;
            }

            @Override
            public void onServerDisconnected(Server server) {
                disconnected++;
            }

            @Override
            public void onActiveServerChanged(Server oldActive, Server newActive) {
                activeServerChanged++;
            }
        });

        fillServerList();
        for(int i = 0; i < rsaKeys.length; i++) {
            Server server = list.getServer(rsaKeys[i]);
            list.listeners.onServerConnected(server);
            list.setActiveServer(server);
            list.listeners.onServerDisconnected(server);
        }
        assertThat(connected == 10, is(true));
        assertThat(disconnected == 10, is(true));
        assertThat(activeServerChanged == 10, is(true));
    }

    private int discovered = 0;
    private int lost = 0;

    @Test
    public void test_ServerListenerFires(){
        list.listeners.addServerListChangeListener(new ServerListeners.ServerListChangeListener() {
            @Override
            public void onServerDiscovered(Server server) {
                discovered++;
            }

            @Override
            public void onServerLost(Server server) {
                lost++;
            }
        });

        fillServerList();
        assertTrue(discovered == 10);
        emptyServerList();
        assertTrue(lost == 10);
    }

    @Test
    public void test_iterateServers(){
        int sCount = 10;
        ServerList sl = ServerList.getInstance();
        int initCount = sl.size();
        List<Server> servers = MockDataFactory.getServer(sCount);

        for (int i = 0; i < sCount; i++) {
            sl.addServer(servers.get(i));
        }

        int loopCount = 0;
        for(Server s : ServerList.getInstance())
        {
            loopCount++;
        }

        Assert.assertTrue(loopCount == initCount + sCount);
    }
}

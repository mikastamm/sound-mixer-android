package mikastamm.com.soundmixer.UI;

import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.KnownServerList;
import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.NetworkDiscoveryBroadcastSender;
import mikastamm.com.soundmixer.R;
import mikastamm.com.soundmixer.ServerList;
import mikastamm.com.soundmixer.ServerListChangeDelegate;

/**
 * Created by Mika on 09.04.2018.
 */

public class ServerPresenter {
    private MainActivity mainActivity;
    private NavigationView navigationView;
    private SubMenu serverMenu;
    private ServerListChangeDelegate.ServerListChangeListener listener;
    NetworkDiscoveryBroadcastSender.NetworkDiscoveryDelegate.NetworkDiscoveryListener ndListener;

    public ServerPresenter(MainActivity activity, NavigationView navigationView) {
        this.mainActivity = activity;
        this.navigationView = navigationView;
        serverMenu = navigationView.getMenu().addSubMenu(activity.getString(R.string.main_menu_servers_submenu));
        addEmptyMenuEntry();
        subscribeToListeners();
    }

    public void dispose() {
        ServerList.getInstance().changeDelegate.removeServerListChangeListener(listener);
        mainActivity.networkDiscoveryBroadcastSender.delegate.removeListener(ndListener);
    }

    private void subscribeToListeners() {
        listener = new ServerListChangeDelegate.ServerListChangeListener() {
            @Override
            public void onServerDiscovered(Server server) {
                final Server finalServer = server;
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addServerToMenu(finalServer);
                        removeEmptyMenuEntry();
                    }
                });
            }

            @Override
            public void onServerLost(Server server) {
                final Server finalServer = server;
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeFromServerMenu(finalServer);
                    }
                });
            }
        };

        ServerList.getInstance().changeDelegate.addServerListChangeListener(listener);

        ndListener = new NetworkDiscoveryBroadcastSender.NetworkDiscoveryDelegate.NetworkDiscoveryListener() {
            @Override
            public void onNetworkDiscoveryStarted() {
                Log.i(MainActivity.TAG, "Network Discovery Started");
                clearServerMenu();
                addEmptyMenuEntry();
            }

            @Override
            public void onNetworkDiscoveryFinished() {
                Log.i(MainActivity.TAG, "Network Discovery Finished");
                boolean hasConnected = false;

                for (Server s : ServerList.getInstance()) {
                    if (KnownServerList.isLastConnected(s.id, mainActivity) && !isConnected()) {
                        mainActivity.connect(s);
                        hasConnected = true;
                    }
                }

                if (!hasConnected)
                    for (Server s : ServerList.getInstance()) {
                        if (KnownServerList.isKnown(s.id, mainActivity) && !isConnected()) {
                            mainActivity.connect(s);
                        }
                    }
            }
        };

        mainActivity.networkDiscoveryBroadcastSender.delegate.addListener(ndListener);
    }

    private boolean isConnected(){
       return ServerList.getInstance().getActiveServer() != null;
    }

    //To keep the "Computers" submenu even though there are no servers
    private void addEmptyMenuEntry(){
        serverMenu.add(Menu.NONE, 0, Menu.NONE, "");
    }

    private void removeEmptyMenuEntry(){
        serverMenu.removeItem(0);
    }

    private void clearServerMenu(){
        serverMenu.clear();
    }

    private void addServerToMenu(Server server) {
        int integerServerId = server.getIntegerServerId();
        MenuItem item = serverMenu.add(Menu.NONE, integerServerId, Menu.NONE, server.name);
        item.setIcon(R.drawable.ic_server);
        Log.i(MainActivity.TAG, "Added " + server.name + " to menu");
    }

    private void removeFromServerMenu(Server server) {
        int integerServerId = server.getIntegerServerId();
        serverMenu.removeItem(integerServerId);
        Log.i(MainActivity.TAG, "Remove " + server.name + " from menu");
    }


}

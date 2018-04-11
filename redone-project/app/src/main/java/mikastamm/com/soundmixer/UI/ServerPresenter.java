package mikastamm.com.soundmixer.UI;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.R;
import mikastamm.com.soundmixer.ServerList;
import mikastamm.com.soundmixer.ServerListeners;

/**
 * Created by Mika on 09.04.2018.
 */

public class ServerPresenter {
    private AppCompatActivity acivity;
    private NavigationView navigationView;
    private SubMenu serverMenu;
    private ServerListeners.ServerListChangeListener listener;

    public ServerPresenter(AppCompatActivity activity, NavigationView navigationView) {
        this.acivity = activity;
        this.navigationView = navigationView;
        serverMenu = navigationView.getMenu().addSubMenu(activity.getString(R.string.main_menu_servers_submenu));
        subscribeToListener();
    }

    public void dispose() {
        ServerList.getInstance().listeners.removeServerListChangeListener(listener);
    }

    private void subscribeToListener() {
        listener = new ServerListeners.ServerListChangeListener() {
            @Override
            public void onServerDiscovered(Server server) {
                final Server finalServer = server;
                acivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addServerToMenu(finalServer);
                    }
                });
            }

            @Override
            public void onServerLost(Server server) {
                final Server finalServer = server;
                acivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeFromServerMenu(finalServer);
                    }
                });
            }
        };

        ServerList.getInstance().listeners.serverListChangeListeners.add(listener);
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

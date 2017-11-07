package com.nulldozer.volumecontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.lucasr.twowayview.TwoWayView;

import java.util.ArrayList;

/**
 * Created by Mika on 03.11.2017.
 */

public class ServerListFragment extends Fragment {

    private final static String TAG = "ServerListFragment";
    private View inflatedView;
    public TwoWayView listViewServers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.servers_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        inflatedView = view;
    }

    public void initialize(){
        MainActivity.Instance.serverListViewAdapter = new ServerListViewAdapter(MainActivity.Instance, new ArrayList<VolumeServer>());
        listViewServers = (TwoWayView)inflatedView.findViewById(R.id.listViewServers);
        listViewServers.setAdapter(MainActivity.Instance.serverListViewAdapter);

        listViewServers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final VolumeServer selectedServer = MainActivity.Instance.serverListViewAdapter.listElements.get(position);

                if (MainActivity.Instance.serverListViewAdapter.activeServer != null && MainActivity.Instance.serverListViewAdapter.activeServer.RSAPublicKey.equals(selectedServer.RSAPublicKey)) {
                    Toast.makeText(MainActivity.Instance, "Already connected to " + selectedServer.name, Toast.LENGTH_SHORT).show();
                } else if (selectedServer.hasPassword && selectedServer.standardPassword.equals("")) {
                    final SharedPreferences prefs = MainActivity.Instance.getPreferences(Context.MODE_PRIVATE);
                    Log.i("MainActivity", "Selected server requires authentification, showing Password Dialog");
                    Log.i(TAG, "Saved Password:" + prefs.getString(PrefKeys.ServerStandardPasswordPrefix + VCCryptography.getMD5Hash(selectedServer.RSAPublicKey), ""));

                    PasswordDialog pwDialog = new PasswordDialog();
                    pwDialog.setServer(selectedServer);
                    pwDialog.show(MainActivity.Instance.getSupportFragmentManager(), "password-dialog");

                } else {
                    MainActivity.Instance.serverListViewAdapter.setActive(selectedServer);
                }
            }
        });

        listViewServers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;

                PopupMenu popup = new PopupMenu(MainActivity.Instance, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.overflow_menu_server, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals(getString(R.string.server_menu_forget))) {
                            SharedPreferences.Editor editor = MainActivity.Instance.getPreferences(Context.MODE_PRIVATE).edit();
                            editor.putString(PrefKeys.ServerStandardPasswordPrefix + VCCryptography.getMD5Hash(MainActivity.Instance.serverListViewAdapter.listElements.get(pos).RSAPublicKey), "");
                            MainActivity.Instance.serverListViewAdapter.listElements.get(pos).standardPassword = "";
                            editor.apply();

                            Log.i(TAG, "Forgot password for " + MainActivity.Instance.serverListViewAdapter.listElements.get(pos).name);
                        } else if (item.getTitle().equals(getString(R.string.server_menu_disconnect))) {
                            KnownServerHelper.forget(MainActivity.Instance.serverListViewAdapter.listElements.get(pos).RSAPublicKey);

                            if(MainActivity.Instance.clientFragment.clientConnection != null)
                                MainActivity.Instance.clientFragment.clientConnection.close();
                        }
                        return true;
                    }
                });

                return true;
            }
        });
    }
}

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

    public void initialize(final MainActivity mainActivity){
        mainActivity.serverListViewAdapter = new ServerListViewAdapter(mainActivity, new ArrayList<VolumeServer>());
        listViewServers = (TwoWayView)inflatedView.findViewById(R.id.listViewServers);
        listViewServers.setAdapter(mainActivity.serverListViewAdapter);

        listViewServers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final VolumeServer selectedServer = mainActivity.serverListViewAdapter.listElements.get(position);

                if (mainActivity.serverListViewAdapter.activeServer != null && mainActivity.serverListViewAdapter.activeServer.RSAPublicKey.equals(selectedServer.RSAPublicKey)) {
                    Toast.makeText(mainActivity, "Already connected to " + selectedServer.name, Toast.LENGTH_SHORT).show();
                } else if (selectedServer.hasPassword && selectedServer.standardPassword.equals("")) {
                    final SharedPreferences prefs = mainActivity.getPreferences(Context.MODE_PRIVATE);
                    Log.i("MainActivity", "Selected server requires authentification, showing Password Dialog");
                    Log.i(TAG, "Saved Password:" + prefs.getString(PrefKeys.ServerStandardPasswordPrefix + VCCryptography.getMD5Hash(selectedServer.RSAPublicKey), ""));

                    PasswordDialog pwDialog = new PasswordDialog();
                    pwDialog.setMainActivity(mainActivity);
                    pwDialog.setServer(selectedServer);
                    pwDialog.show(mainActivity.getSupportFragmentManager(), "password-dialog");

                } else {
                    mainActivity.serverListViewAdapter.setActive(selectedServer);
                }
            }
        });

        listViewServers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;

                PopupMenu popup = new PopupMenu(mainActivity, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.overflow_menu_server, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals(getString(R.string.server_menu_forget))) {
                            SharedPreferences.Editor editor = mainActivity.getPreferences(Context.MODE_PRIVATE).edit();
                            editor.putString(PrefKeys.ServerStandardPasswordPrefix + VCCryptography.getMD5Hash(mainActivity.serverListViewAdapter.listElements.get(pos).RSAPublicKey), "");
                            mainActivity.serverListViewAdapter.listElements.get(pos).standardPassword = "";
                            editor.apply();

                            Log.i(TAG, "Forgot password for " + mainActivity.serverListViewAdapter.listElements.get(pos).name);
                        } else if (item.getTitle().equals(getString(R.string.server_menu_disconnect))) {
                            KnownServerHelper.forget(mainActivity.serverListViewAdapter.listElements.get(pos).RSAPublicKey, mainActivity);

                            if(mainActivity.clientFragment.clientThread != null)
                                mainActivity.clientFragment.clientThread.close();
                        }
                        return true;
                    }
                });

                return true;
            }
        });
    }
}

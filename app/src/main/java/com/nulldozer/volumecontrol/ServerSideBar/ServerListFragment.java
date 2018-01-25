package com.nulldozer.volumecontrol.ServerSideBar;

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
import android.widget.PopupMenu;
import android.widget.Toast;

import com.nulldozer.volumecontrol.KnownServerHelper;
import com.nulldozer.volumecontrol.MainActivity;
import com.nulldozer.volumecontrol.PasswordDialog;
import com.nulldozer.volumecontrol.PrefKeys;
import com.nulldozer.volumecontrol.R;
import com.nulldozer.volumecontrol.VCCryptography;
import com.nulldozer.volumecontrol.VolumeServer;

import org.lucasr.twowayview.TwoWayView;

import java.util.ArrayList;

/**
 * Created by Mika on 03.11.2017.
 */

public class ServerListFragment extends Fragment {

    private final static String TAG = "ServerListFragment";
    private View inflatedView;
    public TwoWayView listViewServers;
    private ServerListViewAdapter adapter;
    
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
        adapter = new ServerListViewAdapter(mainActivity, new ArrayList<VolumeServer>());
        listViewServers = (TwoWayView)inflatedView.findViewById(R.id.listViewServers);
        listViewServers.setAdapter(adapter);

        listViewServers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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
                            editor.putString(PrefKeys.ServerStandardPasswordPrefix + VCCryptography.getMD5Hash(adapter.listElements.get(pos).RSAPublicKey), "");
                            adapter.listElements.get(pos).standardPassword = "";
                            editor.apply();

                            Log.i(TAG, "Forgot password for " + adapter.listElements.get(pos).name);
                        } else if (item.getTitle().equals(getString(R.string.server_menu_disconnect))) {
                            KnownServerHelper.forget(adapter.listElements.get(pos).RSAPublicKey, mainActivity);

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

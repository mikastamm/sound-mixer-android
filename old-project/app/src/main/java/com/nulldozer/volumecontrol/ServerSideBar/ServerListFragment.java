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

    private ArrayList<VolumeServer> servers = new ArrayList<VolumeServer>();
    private VolumeServer activeServer;

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

    public void setActive(VolumeServer server)
    {
        removeActive();
        if(servers.contains(server))
        {
            activeServer = server;
            //TODO:connect(activeServer)
        }
    }

    public void removeActive()
    {
        //TODO: Disconnect
    }

    public VolumeServer getElement(int element)
    {
        return servers.get(element);
    }

    public boolean hasActive() {
        return activeServer == null;
    }

    public VolumeServer getActive(){
        return activeServer;
    }

    public void initialize(final MainActivity mainActivity){
        adapter = new ServerListViewAdapter(mainActivity, servers);
        listViewServers = (TwoWayView)inflatedView.findViewById(R.id.listViewServers);
        listViewServers.setAdapter(adapter);

        listViewServers.setOnItemClickListener(new ServerListClickListenerImp(mainActivity,this));
        listViewServers.setOnItemLongClickListener(new ServerListLongClickListenerImp(mainActivity, this));
    }


}

package com.nulldozer.volumecontrol;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by Mika on 02.11.2017.
 */

public class NetworkEventHandlers {

    private static String TAG = "NetworkEventHandlers";

    public ListViewAdapter seekbarAdapter;
    public ServerListViewAdapter serverListViewAdapter;

    public NetworkEventHandlers(ListViewAdapter seekbarAdapter, ServerListViewAdapter serverListViewAdapter)
    {
        this.seekbarAdapter = seekbarAdapter;
        this.serverListViewAdapter = serverListViewAdapter;
    }

    public void onNetworkDiscoveryStarted(){
        TextView serverCountTextView = ((TextView) MainActivity.Instance.findViewById(R.id.tvServerCount));

        if (Build.VERSION.SDK_INT >= 19 && !Settings.useAlternativeServerRefresh)
            MainActivity.Instance.serverRefreshByUser.swipeContainer.setRefreshing(true);

        serverCountTextView.setText("0 Servers");
    }

    public void onNetworkDiscoveryFinished(){

        if (Build.VERSION.SDK_INT >= 19 && !Settings.useAlternativeServerRefresh)
            MainActivity.Instance.serverRefreshByUser.swipeContainer.setRefreshing(false);

        if(serverListViewAdapter.listElements.size() == 0)
        {
            if(Build.VERSION.SDK_INT < 19 || Settings.useAlternativeServerRefresh)
                MainActivity.Instance.serverRefreshByUser.refreshServersTip.setEnabled(true);

            MainActivity.Instance.serverRefreshByUser.refreshServersTip.setVisibility(View.VISIBLE);

            if(!MainActivity.Instance.sidebarController.sideBarExpanded && !MainActivity.Instance.fragmentRetained)
                MainActivity.Instance.sidebarController.toggleSidebar();
        }

        if(serverListViewAdapter.activeServer == null && !MainActivity.Instance.sidebarController.sideBarExpanded)
            MainActivity.Instance.sidebarController.toggleSidebar();
    }

    public void onServerDiscovered(VolumeServer server){
        Log.i(TAG, "Found Server:" + server.toString());

        if(serverListViewAdapter.activeServer != null && serverListViewAdapter.activeServer.RSAPublicKey.equals(server.RSAPublicKey))
        {
            serverListViewAdapter.listElements.add(serverListViewAdapter.activeServer);
        }
        else{
            serverListViewAdapter.listElements.add(server);
        }
        serverListViewAdapter.notifyDataSetChanged();

        TextView serverCountTextView = ((TextView)MainActivity.Instance.findViewById(R.id.tvServerCount));
        serverCountTextView.setText(serverListViewAdapter.getCount() + (serverListViewAdapter.getCount() == 1 ? " Server" : " Servers"));

        MainActivity.Instance.serverRefreshByUser.refreshServersTip.setVisibility(View.GONE);
    }

    public void onConnect(VolumeServer target){

    }

    public void onAudioSessionListReceived(VolumeServer target, VolumeData[] audioSessions)
    {
        LinearLayout llConnectionTip = (LinearLayout)MainActivity.Instance.findViewById(R.id.llConnectionTip);
        llConnectionTip.setVisibility(View.GONE);

        if (!MainActivity.Instance.fragmentRetained)
            Toast.makeText(MainActivity.Instance, "Connected to " + target.name, Toast.LENGTH_SHORT).show();

        if(MainActivity.Instance.sidebarController.sideBarExpanded && !MainActivity.Instance.fragmentRetained)
        {
            MainActivity.Instance.sidebarController.toggleSidebar();
        }

        //Fill the listViewVolumeSliders with the received data
        MainActivity.Instance.listViewAdapterVolumeSliders.clear();
        MainActivity.Instance.listViewAdapterVolumeSliders.addAll(audioSessions);

        MainActivity.Instance.listViewAdapterVolumeSliders.notifyDataSetChanged();
        for (int i = 0; i < seekbarAdapter.listElements.size(); i++) {
            VolumeData vm = seekbarAdapter.listElements.get(i);
            if (vm.title.equals("Master")) //TODO: Use multilanguage title
            {
                if (i != 0) {
                    VolumeData pos0 = seekbarAdapter.listElements.get(0);
                    seekbarAdapter.listElements.set(0, vm);
                    seekbarAdapter.listElements.set(i, pos0);
                }
            } else if (vm.title.equals("System"))//TODO: Use multilanguage title
            {
                if (i != 1) {
                    VolumeData pos1 = seekbarAdapter.listElements.get(1);
                    seekbarAdapter.listElements.set(1, vm);
                    seekbarAdapter.listElements.set(i, pos1);
                }
            }
        }
        seekbarAdapter.notifyDataSetChanged();
    }
}

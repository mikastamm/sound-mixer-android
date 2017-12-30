package com.nulldozer.volumecontrol;

import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by Mika on 02.11.2017.
 */

public class NetworkEventHandlers {

    private static String TAG = "NetworkEventHandlers";

    private ListViewAdapter seekbarAdapter;
    private ServerListViewAdapter serverListViewAdapter;
    private MainActivity mainActivity;
    
    public NetworkEventHandlers(MainActivity mainActivity, ListViewAdapter seekbarAdapter, ServerListViewAdapter serverListViewAdapter)
    {
        this.mainActivity = mainActivity;
        this.seekbarAdapter = seekbarAdapter;
        this.serverListViewAdapter = serverListViewAdapter;
    }

    public void onServerDisconnected(){
        Log.i(TAG, "onServerDisconnected()");
        new NetworkDiscoveryThread(mainActivity).start();
    }

    public void onNetworkDiscoveryStarted(boolean silent){
        TextView serverCountTextView = ((TextView) mainActivity.findViewById(R.id.tvServerCount));

        if (!silent && Build.VERSION.SDK_INT >= 19 && !Settings.useAlternativeServerRefresh)
            mainActivity.serverRefreshByUser.swipeContainer.setRefreshing(true);

        serverCountTextView.setText("0 Servers");

        if(mainActivity.serverListViewAdapter.activeServer == null) {
            ProgressBar pbConnecting = (ProgressBar) mainActivity.findViewById(R.id.pbConnecting);
            pbConnecting.setVisibility(View.GONE);
        }

        if(mainActivity.serverListViewAdapter.activeServer == null) {
            LinearLayout llConnectionTip = (LinearLayout) mainActivity.findViewById(R.id.llConnectionTip);
            llConnectionTip.setVisibility(View.GONE);
        }
    }

    public void onNetworkDiscoveryFinished(){
        ProgressBar pbConnecting = (ProgressBar) mainActivity.findViewById(R.id.pbConnecting);
        pbConnecting.setVisibility(View.GONE);

        if(serverListViewAdapter.listElements.size() == 0) {
            LinearLayout llConnectionTip = (LinearLayout) mainActivity.findViewById(R.id.llConnectionTip);
            llConnectionTip.setVisibility(View.VISIBLE);
        }

        if (Build.VERSION.SDK_INT >= 19 && !Settings.useAlternativeServerRefresh)
            mainActivity.serverRefreshByUser.swipeContainer.setRefreshing(false);

        if(serverListViewAdapter.listElements.size() == 0)
        {
            if(Build.VERSION.SDK_INT < 19 || Settings.useAlternativeServerRefresh)
                mainActivity.serverRefreshByUser.refreshServersTip.setEnabled(true);

            mainActivity.serverRefreshByUser.refreshServersTip.setVisibility(View.VISIBLE);

        }

        if(serverListViewAdapter.activeServer == null && !mainActivity.sidebarController.sideBarExpanded)
            mainActivity.sidebarController.toggleSidebar();
    }

    public void onServerDiscovered(final VolumeServer server){
        Log.i(TAG, "Found Server:" + server.toString());

        LinearLayout llConnectionTip = (LinearLayout)mainActivity.findViewById(R.id.llConnectionTip);
        llConnectionTip.setVisibility(View.GONE);

        final SharedPreferences prefs = mainActivity.getPreferences(MainActivity.MODE_PRIVATE);
        server.standardPassword = prefs.getString(PrefKeys.ServerStandardPasswordPrefix + VCCryptography.getMD5Hash(server.RSAPublicKey), "");

        if(server.IPAddress.contains(":"))
        {
            server.IPAddress = server.IPAddress.substring(0, server.IPAddress.indexOf(":"));
        }

        if(server.IPAddress.startsWith("/"))
        {
            server.IPAddress=server.IPAddress.substring(1);
        }

        if(serverListViewAdapter.activeServer != null && serverListViewAdapter.activeServer.RSAPublicKey.equals(server.RSAPublicKey))
        {
            serverListViewAdapter.listElements.add(serverListViewAdapter.activeServer);
        }
        else{
            serverListViewAdapter.listElements.add(server);
        }
        serverListViewAdapter.notifyDataSetChanged();

        TextView serverCountTextView = ((TextView)mainActivity.findViewById(R.id.tvServerCount));
        serverCountTextView.setText(serverListViewAdapter.getCount() + (serverListViewAdapter.getCount() == 1 ? " Server" : " Servers"));

        mainActivity.serverRefreshByUser.refreshServersTip.setVisibility(View.GONE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PrefKeys.FirstConnectHappened, true);
        editor.apply();

        //Connect to the found Server if not connected and allowed in settings
        if(mainActivity.serverListViewAdapter.activeServer == null)
        {
            VolumeServer passwordLessServer;

            if(Settings.autoConnectToLastConnectedServer && KnownServerHelper.isKnown(server.RSAPublicKey, mainActivity)){
                mainActivity.serverListViewAdapter.setActive(server);
            }
            else if(Settings.autoConnectToServersWithoutPassword && (passwordLessServer = mainActivity.serverListViewAdapter.getPasswordlessServer()) != null)
            {
                mainActivity.serverListViewAdapter.setActive(passwordLessServer);
            }

        }
    }

    public void onConnectionInitiated(VolumeServer target){
        ProgressBar pbConnecting = (ProgressBar) mainActivity.findViewById(R.id.pbConnecting);
        pbConnecting.setVisibility(View.VISIBLE);
    }

    public void onAudioSessionListReceived(VolumeServer target, VolumeData[] audioSessions)
    {
        KnownServerHelper.addToKnown(target.RSAPublicKey, mainActivity);

        ProgressBar pbConnecting = (ProgressBar) mainActivity.findViewById(R.id.pbConnecting);
        pbConnecting.setVisibility(View.GONE);

        LinearLayout llConnectionTip = (LinearLayout)mainActivity.findViewById(R.id.llConnectionTip);
        llConnectionTip.setVisibility(View.GONE);

        if (!mainActivity.fragmentRetained)
            Toast.makeText(mainActivity, "Connected to " + target.name, Toast.LENGTH_SHORT).show();

        //Fill the listViewVolumeSliders with the received data
        mainActivity.listViewAdapterVolumeSliders.clear();
        mainActivity.listViewAdapterVolumeSliders.addAll(audioSessions);

        mainActivity.listViewAdapterVolumeSliders.notifyDataSetChanged();
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

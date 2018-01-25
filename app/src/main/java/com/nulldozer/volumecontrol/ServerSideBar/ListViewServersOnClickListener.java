package com.nulldozer.volumecontrol.ServerSideBar;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.nulldozer.volumecontrol.PasswordDialog;
import com.nulldozer.volumecontrol.PrefKeys;
import com.nulldozer.volumecontrol.VCCryptography;
import com.nulldozer.volumecontrol.VolumeServer;

/**
 * Created by Mika on 25.01.2018.
 */

public class ListViewServersOnClickListener implements AdapterView.OnItemClickListener {

    private String TAG = "ListViewServersOnClickListener";
    private Context mainContext;

    public ListViewServersOnClickListener(Context mainContext, ServerListViewAdapter adapter){
        this.mainContext = mainContext;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
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
}

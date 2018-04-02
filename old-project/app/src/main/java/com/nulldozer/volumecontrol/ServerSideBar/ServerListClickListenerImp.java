package com.nulldozer.volumecontrol.ServerSideBar;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.nulldozer.volumecontrol.MainActivity;
import com.nulldozer.volumecontrol.PasswordDialog;
import com.nulldozer.volumecontrol.PrefHelper;
import com.nulldozer.volumecontrol.PrefKeys;
import com.nulldozer.volumecontrol.VCCryptography;
import com.nulldozer.volumecontrol.VolumeServer;

import java.lang.ref.WeakReference;

/**
 * Created by Mika on 25.01.2018.
 */

public class ServerListClickListenerImp implements AdapterView.OnItemClickListener {

    private String TAG = "ServerListClickListenerImp";
    private WeakReference<MainActivity> mainActivityRef;
    private WeakReference<ServerListFragment> fragmentRef;

    public ServerListClickListenerImp(MainActivity mainActivity, ServerListFragment fragment){
        this.mainActivityRef = new WeakReference<MainActivity>(mainActivity);
        this.fragmentRef = new WeakReference<ServerListFragment>(fragment);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        MainActivity mainActivity = mainActivityRef.get();
        ServerListFragment fragment = fragmentRef.get();
        if(mainActivity != null && fragment != null) {
            final VolumeServer selectedServer = fragment.getElement(position);

            if (fragment.hasActive() && fragment.getActive().equals(selectedServer)) {
                Toast.makeText(mainActivity, "Already connected to " + selectedServer.name, Toast.LENGTH_SHORT).show();
            } else if (selectedServer.hasPassword && selectedServer.standardPassword.equals("")) {
                Log.i("MainActivity", "Selected server requires authentification, showing Password Dialog");
                Log.i(TAG, "Saved Password:" + PrefHelper.getStringPreference(mainActivity, PrefKeys.ServerStandardPasswordPrefix + VCCryptography.getMD5Hash(selectedServer.RSAPublicKey), ""));

                PasswordDialog pwDialog = new PasswordDialog();
                pwDialog.setMainActivity(mainActivity);
                pwDialog.setServer(selectedServer);
                pwDialog.show(mainActivity.getSupportFragmentManager(), "password-dialog");

            } else {
                fragment.setActive(selectedServer);
            }
        }
    }
}

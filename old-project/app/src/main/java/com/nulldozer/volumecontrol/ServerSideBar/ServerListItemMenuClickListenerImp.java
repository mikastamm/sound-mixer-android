package com.nulldozer.volumecontrol.ServerSideBar;

import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.nulldozer.volumecontrol.KnownServerHelper;
import com.nulldozer.volumecontrol.PrefHelper;
import com.nulldozer.volumecontrol.PrefKeys;
import com.nulldozer.volumecontrol.R;
import com.nulldozer.volumecontrol.VCCryptography;
import com.nulldozer.volumecontrol.VolumeServer;

public class ServerListItemMenuClickListenerImp implements PopupMenu.OnMenuItemClickListener {

    int position;
    Activity activity;
    View showAtView;
    ServerListFragment serverListFragment;

    public ServerListItemMenuClickListenerImp(final int position, final Activity activity, View showAtView, final ServerListFragment serverListFragment)
    {
        this.position = position;
        this.activity = activity;
        this.showAtView = showAtView;
        this.serverListFragment = serverListFragment;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        VolumeServer selected = serverListFragment.getElement(position);

        if (item.getTitle().equals(activity.getString(R.string.server_menu_forget))) {
            //forgetPassword
        }
        else if (item.getTitle().equals(activity.getString(R.string.server_menu_disconnect))) {
            KnownServerHelper.forget(selected.RSAPublicKey, activity);

            serverListFragment.removeActive();
        }
        return true;
    }
}

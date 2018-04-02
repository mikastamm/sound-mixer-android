package com.nulldozer.volumecontrol.ServerSideBar;

import android.app.Activity;
import android.view.MenuInflater;
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

/**
 * Created by Mika on 27.01.2018.
 */

public class SideBarBehavior {
    public static void showServerMenu(final int position, final Activity activity, View showAtView, final ServerListFragment serverListFragment)
    {
        PopupMenu popup = new PopupMenu(activity, showAtView);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.overflow_menu_server, popup.getMenu());
        popup.show();

        popup.setOnMenuItemClickListener(new ServerListItemMenuClickListenerImp(position, activity,showAtView, serverListFragment));

    }




}



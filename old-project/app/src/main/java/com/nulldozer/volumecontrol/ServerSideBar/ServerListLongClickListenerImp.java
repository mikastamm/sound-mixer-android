package com.nulldozer.volumecontrol.ServerSideBar;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.nulldozer.volumecontrol.KnownServerHelper;
import com.nulldozer.volumecontrol.MainActivity;
import com.nulldozer.volumecontrol.PrefHelper;
import com.nulldozer.volumecontrol.PrefKeys;
import com.nulldozer.volumecontrol.R;
import com.nulldozer.volumecontrol.VCCryptography;
import com.nulldozer.volumecontrol.VolumeServer;

import java.lang.ref.WeakReference;

/**
 * Created by Mika on 26.01.2018.
 */

public class ServerListLongClickListenerImp implements AdapterView.OnItemLongClickListener {

    private String TAG = "ServerListLongClickListenerImp";
    private WeakReference<MainActivity> mainActivityRef;
    private WeakReference<ServerListFragment> fragmentRef;

    public ServerListLongClickListenerImp(MainActivity mainActivity, ServerListFragment fragment){
        this.mainActivityRef = new WeakReference<MainActivity>(mainActivity);
        this.fragmentRef = new WeakReference<ServerListFragment>(fragment);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        SideBarBehavior.showServerMenu(position, mainActivityRef.get(), view, fragmentRef.get());
        return true;
    }
}

package com.nulldozer.volumecontrol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.lucasr.twowayview.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String keyRestoreSidebarOpen = "SidebarOpen";

    //<Fragment Tags>
    public static String TagClientFragment = "CLIENT_FRAGMENT";
    //</Fragment Tags>

    public ListViewAdapter listViewAdapterVolumeSliders;
    public ServerListViewAdapter serverListViewAdapter;

    TwoWayView listViewVolumeSliders;

    public static MainActivity Instance;
    public ClientFragment clientFragment;

    boolean passwordDialogOpen = false;
    SettingsManager settingsManager;
    private static final String TAG = "MainActivity";

    FragmentManager fragmentManager;
    boolean fragmentRetained = false;

    NetworkEventHandlers networkEventHandlers;
    Fullscreen fullscreen;
    OrientationManager orientationManager;
    SidebarController sidebarController;
    ServerRefreshByUser serverRefreshByUser;
    BroadcastReceiverThread broadcastReceiver;
    ServerListFragment serverListFragment;

    @Override
    public void onPause() {
        super.onPause();
        // this means that this activity will not be recreated now, user is leaving it
        // or the activity is otherwise finishing
        if(isFinishing()) {
            // we will not need this fragment anymore, this may also be a good place to signal
            // to the retained fragment object to perform its own cleanup.
            fragmentManager.beginTransaction().remove(clientFragment).commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fullscreen = new Fullscreen(this);
        if(Settings.useFullscreen)
        {
            fullscreen.enable();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Instance = this;
        LaunchCounter.launched(this);
        VCCryptography.generateRSAKeyPair(false);

        fragmentManager = getFragmentManager();

        //Add the Actionbar and hide it
        android.support.v7.app.ActionBar supActionBar;
        if((supActionBar = getSupportActionBar()) != null)
        {
            supActionBar.hide();
        }

        //Volume Slider Adapter Init
        listViewAdapterVolumeSliders = new ListViewAdapter(this, new ArrayList<VolumeData>());
        listViewVolumeSliders = (TwoWayView) findViewById(R.id.lvVolumeSliders);
        listViewVolumeSliders.setAdapter(listViewAdapterVolumeSliders);

        serverListFragment = (ServerListFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentServerList);
        serverListFragment.initialize();

        clientFragment = getClientFragment();
        if(!fragmentRetained)
            new NetworkDiscoveryThread().start();

        broadcastReceiver = new BroadcastReceiverThread();
        broadcastReceiver.start();

        serverRefreshByUser = new ServerRefreshByUser(this);
        networkEventHandlers = new NetworkEventHandlers(listViewAdapterVolumeSliders, serverListViewAdapter);

        //Show the Rate Us prompt if conditions are met
        final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        if(prefs.getBoolean(PrefKeys.FirstConnectHappened_PrefKey, false) && !fragmentRetained)
        {
            new RatePrompt(this).tryShow();
        }

        //Show the Install instructions if conditions are met
        if(prefs.getBoolean(PrefKeys.ShowInstallInstructionsOnStart_PrefKey, true))
        {
           showInstructionsDialog();
        }

        Nightmode.setEnabled(this, Settings.nightmode);
        settingsManager = new SettingsManager();
        orientationManager = new OrientationManager(this);
        sidebarController = new SidebarController(this, orientationManager.isLandscape);

        Button btnTryAgain = (Button)findViewById(R.id.btnTryAgain);
        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NetworkDiscoveryThread().start();
            }
        });

        final ImageView expandImg = (ImageView) findViewById(R.id.expandImg);
        if(orientationManager.isLandscape)
        {
            listViewVolumeSliders.setOrientation(TwoWayView.Orientation.HORIZONTAL);
            serverListFragment.listViewServers.setOrientation(TwoWayView.Orientation.VERTICAL);
            expandImg.setRotation(0);
        }
        else{
            listViewVolumeSliders.setOrientation(TwoWayView.Orientation.VERTICAL);
            serverListFragment.listViewServers.setOrientation(TwoWayView.Orientation.HORIZONTAL);
            expandImg.setRotation(90);
        }

        if(savedInstanceState != null)
        {
            if(savedInstanceState.getBoolean(keyRestoreSidebarOpen, false))
            {
                if(!sidebarController.sideBarExpanded)
                sidebarController.toggleSidebar();
            }
        }
        //RepopulateVolumeDataListViewAdapter(new VolumeData[]{new VolumeData("Master", .95f, false, -1, "0"), new VolumeData("System", .65f, false, 0, "1"), new VolumeData("Firefox", .95f, false, 1, "2"), new VolumeData("Spotify", .30f, false, 3, "3"), new VolumeData("Steam", .10f, false, 5, "66")});
        //RefreshServerListValues(new VolumeServer[]{new VolumeServer(false, "Mika-PC", "192.168.0.122"), new VolumeServer(true, "SERVER", "192.168.0.117", "123"), new VolumeServer(false, "Work-PC", "192.168.0.217", "3rt4"), new VolumeServer(true, "SERVER1", "192.168.0.118"), new VolumeServer(true, "SERVER2", "192.168.0.119")});
    }

    @Override
    protected void onSaveInstanceState (Bundle outState)
    {
        outState.putBoolean(keyRestoreSidebarOpen, sidebarController.sideBarExpanded);
        super.onSaveInstanceState(outState);
    }

    public ClientFragment getClientFragment() {
        ClientFragment fragment;
        if(fragmentManager != null)
        {
            fragment = (ClientFragment)fragmentManager.findFragmentByTag(TagClientFragment);

            if(fragment != null)
            {
                serverListViewAdapter.activeServer = fragment.clientThread.activeServer;
                serverListViewAdapter.activeServer.active = true;

                serverListViewAdapter.notifyDataSetChanged();
                new Thread(fragment.clientThread.requestAllAudioSessionsFromServer).start();
                LinearLayout llConnectionTip = (LinearLayout)MainActivity.Instance.findViewById(R.id.llConnectionTip);
                //llConnectionTip.setVisibility(View.GONE);
                fragmentRetained = true;
                return fragment;
            }
        }
        fragmentRetained = false;
        fragment = new ClientFragment();
        fragment.clientThread = new ClientThread();
        if(fragmentManager != null)
        {
            fragmentManager.beginTransaction().add(fragment, TagClientFragment).commit();
        }
        return fragment;
    }

    public void showInstructionsDialog(){
        final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        final Dialog dialog = new Dialog(MainActivity.Instance);
        dialog.setTitle("How to use");
        dialog.setContentView(R.layout.how_to_use_dialog);
        dialog.show();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PrefKeys.ShowInstallInstructionsOnStart_PrefKey, false);
        editor.apply();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    VolumeData masterVolume = listViewAdapterVolumeSliders.getMasterVolumeData();
                    if(masterVolume.volume < 0.9)
                    {
                        masterVolume.volume += 0.1;
                    }
                    else{
                        masterVolume.volume = 1;
                    }
                    clientFragment.clientThread.sendVolumeData(masterVolume);
                }

                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    VolumeData masterVolume = listViewAdapterVolumeSliders.getMasterVolumeData();
                    if(masterVolume.volume > 0.1)
                    {
                        masterVolume.volume -= 0.1;
                    }
                    else{
                        masterVolume.volume = 0;
                    }
                    clientFragment.clientThread.sendVolumeData(masterVolume);
                }

                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Settings.useFullscreen) {
            fullscreen.enable();
        }
    }


    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }


}

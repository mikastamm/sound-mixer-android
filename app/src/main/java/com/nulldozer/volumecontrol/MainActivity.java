package com.nulldozer.volumecontrol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
    TwoWayView listViewServers;

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

        initializeServerListView();

        clientFragment = getClientFragment();
        if(!fragmentRetained)
            new NetworkDiscoveryThread().start();

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

        final ImageView expandImg = (ImageView) findViewById(R.id.expandImg);
        if(orientationManager.isLandscape)
        {
            listViewVolumeSliders.setOrientation(TwoWayView.Orientation.HORIZONTAL);
            listViewServers.setOrientation(TwoWayView.Orientation.VERTICAL);
            expandImg.setRotation(0);
        }
        else{
            listViewVolumeSliders.setOrientation(TwoWayView.Orientation.VERTICAL);
            listViewServers.setOrientation(TwoWayView.Orientation.HORIZONTAL);
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Settings.useFullscreen) {
            fullscreen.enable();
        }
    }

    public void initializeServerListView(){
        serverListViewAdapter = new ServerListViewAdapter(this, new ArrayList<VolumeServer>());

        listViewServers = (TwoWayView) findViewById(R.id.listViewServers);
        listViewServers.setAdapter(serverListViewAdapter);

        listViewServers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;

                PopupMenu popup = new PopupMenu(MainActivity.Instance, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.overflow_menu_server, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals(getString(R.string.server_menu_forget))) {
                            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                            editor.putString(PrefKeys.ServerStandardPasswordPrefix_PrefKey + VCCryptography.getMD5Hash(serverListViewAdapter.listElements.get(pos).RSAPublicKey), "");
                            serverListViewAdapter.listElements.get(pos).standardPassword = "";
                            editor.apply();

                            Log.i(TAG, "Forgot password for " + serverListViewAdapter.listElements.get(pos).name);
                        } else if (item.getTitle().equals(getString(R.string.server_menu_disconnect))) {
                            MainActivity.Instance.serverListViewAdapter.removeActive();
                            MainActivity.Instance.listViewAdapterVolumeSliders.clear();

                            SharedPreferences.Editor editor = MainActivity.Instance.getPreferences(MainActivity.MODE_PRIVATE).edit();
                            editor.putString(PrefKeys.LastConnectedServer_PrefKey, null);
                            editor.apply();
                        }
                        return true;
                    }
                });

                return true;
            }
        });

        listViewServers.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final VolumeServer selectedServer = serverListViewAdapter.listElements.get(position);
                final int finalPosition = position;

                if (MainActivity.Instance.serverListViewAdapter.getActive().equals(selectedServer.RSAPublicKey)) {
                    Toast.makeText(MainActivity.Instance, "Already connected to " + selectedServer.name, Toast.LENGTH_SHORT).show();
                } else if (selectedServer.hasPassword && selectedServer.standardPassword.equals("")) {
                    final SharedPreferences prefs = MainActivity.Instance.getPreferences(MODE_PRIVATE);
                    Log.i("MainActivity", "Selected server requires authentification, showing Password Dialog");
                    Log.i(TAG, "Saved Password:" + prefs.getString(PrefKeys.ServerStandardPasswordPrefix_PrefKey + VCCryptography.getMD5Hash(selectedServer.RSAPublicKey), ""));
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.Instance);
                    builder.setTitle("Password");

                    // Set up the input
                    final EditText input = new EditText(MainActivity.Instance);
                    FrameLayout container = new FrameLayout(MainActivity.Instance);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(getResources().getDimensionPixelSize(R.dimen.edit_text_margin), 0, getResources().getDimensionPixelSize(R.dimen.edit_text_margin), 0);
                    input.setLayoutParams(params);

                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i("MainActivity", "Password entered");


                            SharedPreferences.Editor prefEditor = prefs.edit();

                            selectedServer.standardPassword = input.getText().toString();
                            prefEditor.putString(PrefKeys.ServerStandardPasswordPrefix_PrefKey + VCCryptography.getMD5Hash(selectedServer.RSAPublicKey), selectedServer.standardPassword);
                            prefEditor.apply();

                            passwordDialogOpen = false;
                            serverListViewAdapter.setActive(finalPosition);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i("MainActivity", "Password dialog Canceled");
                            passwordDialogOpen = false;
                            dialog.cancel();
                        }
                    });

                    builder.show();
                    passwordDialogOpen = true;

                } else {
                    serverListViewAdapter.setActive(position);
                }


            }
        });
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
package com.nulldozer.volumecontrol;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.lucasr.twowayview.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static int NETWORK_DISCOVERY_UDP_PORT = 11045;
    public static int NETWORK_DISCOVERY_TCP_PORT = NETWORK_DISCOVERY_UDP_PORT;
    public static int SERVER_CONNECTION_TCP_PORT = 11047;

    public static final String keyRestoreSidebarOpen = "SidebarOpen";

    public static final String RSAPublicKey_PrefKey = "publicKey";
    public static final String RSAPrivateKey_PrefKey = "privateKey";
    public static final String ServerStandardPasswordPrefix_PrefKey = "SPW_";
    public static final String LastConnectedServer_PrefKey = "LastServerPGP";
    public static final String FirstConnectHappened_PrefKey = "FirstConnect";
    public static final String ShowInstallInstructionsOnStart_PrefKey = "FirstLaunch";
    public static final String LaunchCounterPrefKey = "Launches";
    public static final String RatePromptCounterPrefKey = "RatePrompts";

    //<settingsVars>
    public static boolean showServerIpInServerBrowser = false;
    public static boolean useAlternativeServerRefresh = false;
    public static boolean hideApplicationIcons = false;
    public static boolean useFullscreen = false;
    public static boolean nightmode = false;
    public static boolean autoConnectToServersWithoutPassword = false;
    public static boolean autoConnectToLastConnectedServer = true;
    public static boolean reduceSliderSensitivity = false;
    public static Orientation appOrientation = Orientation.AUTO;
    //  <advanced_settingVars>
    public static float maximalResponseTimeForServerInSeconds = 3.0f;
    //  </advanced_settingVars>

    public enum Orientation{
        AUTO,
        PORTRAIT,
        LANDSCAPE
    }

    //</settingVars>

    //<Icon Res IDs>
    public static int mute_icon_res_id;
    public static int master_icon_res_id;
    public static int system_icon_res_id;
    public static int popup_menu_icon_res_id;
    public static int pull_down_icon_res_id;
    public static int application_icon_res_id;
    public static int expand_right_icon_res_id;
    public static Drawable seekbar_progress_drawable;
    //</Icon Res IDs>

    //<Fragment Tags>
    public static String TagClientFragment = "CLIENT_FRAGMENT";
    //</Fragment Tags>

    boolean sideBarExpanded = false;
    public ListViewAdapter adapter;
    public ServerListViewAdapter serverListViewAdapter;

    TwoWayView listView;
    TwoWayView listViewServers;
    boolean isLandscape;

    public static MainActivity Instance;
    public ClientFragment clientFragment;

    public static View refreshServersTip;
    SwipeRefreshLayout swipeContainer;
    boolean passwordDialogOpen = false;
    SettingsManager settingsManager;
    private static final String TAG = "MainActivity";

    FragmentManager fragmentManager;
    boolean fragmentRetained = false;

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

    public void onNetworkDiscoveryFinished(){

        if (Build.VERSION.SDK_INT >= 19 && !useAlternativeServerRefresh)
            swipeContainer.setRefreshing(false);

        if(serverListViewAdapter.listElements.size() == 0)
        {
            if(Build.VERSION.SDK_INT < 19 || useAlternativeServerRefresh)
                refreshServersTip.setEnabled(true);

            refreshServersTip.setVisibility(View.VISIBLE);

            if(!sideBarExpanded && !fragmentRetained)
                toggleSidebar();
        }

        if(serverListViewAdapter.activeServer == null && !sideBarExpanded)
            toggleSidebar();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(useFullscreen)
        {
            if(Build.VERSION.SDK_INT < 19) {
                this.requestWindowFeature(Window.FEATURE_NO_TITLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            else{
                hideSystemUI(getWindow().getDecorView());
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Instance = this;

        fragmentManager = getFragmentManager();

        seekbar_progress_drawable = getResources().getDrawable(R.drawable.seekbar_progressbar);
        VCCryptography.generateRSAKeyPair(false);

        ActionBar actionBar;
        android.support.v7.app.ActionBar supActionBar;
        if((supActionBar = getSupportActionBar()) != null)
        {
            supActionBar.hide();
        }
        if((actionBar = getActionBar()) != null)
        {
            actionBar.hide();
        }

        // Create the adapter to convert the array to views
        adapter = new ListViewAdapter(this, new ArrayList<VolumeData>());
        // Attach the adapter to a ListView

        listView = (TwoWayView) findViewById(R.id.lvItems);
        listView.setAdapter(adapter);

        initializeServerListView();
        new NetworkDiscoveryThread().start();
        clientFragment = getClientFragment();

        initializeSidebar();
        initializeServerRefresh();

        final SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        int launches = prefs.getInt(LaunchCounterPrefKey, 0)+1;
        editor.putInt(LaunchCounterPrefKey, launches);

        if(prefs.getBoolean(FirstConnectHappened_PrefKey, false) && !fragmentRetained)
        {
            int prompts = prefs.getInt(RatePromptCounterPrefKey, 0);
            if(launches > 5 && prompts == 0)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(getLayoutInflater().inflate(R.layout.rate_app_prompt, null));
                
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                editor.putInt(RatePromptCounterPrefKey, prompts+1);
            }
        }

        editor.apply();

        if(prefs.getBoolean(ShowInstallInstructionsOnStart_PrefKey, true))
        {
           showInstructionsDialog();
        }



        settingsManager = new SettingsManager();

        if(appOrientation == Orientation.AUTO) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            isLandscape = getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE;
        }
        else if(appOrientation == Orientation.PORTRAIT)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isLandscape = false;
        }
        else if(appOrientation == Orientation.LANDSCAPE)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            isLandscape = true;
        }

        final ImageView expandImg = (ImageView) findViewById(R.id.expandImg);
        if(isLandscape)
        {
            listView.setOrientation(TwoWayView.Orientation.HORIZONTAL);
            listViewServers.setOrientation(TwoWayView.Orientation.VERTICAL);
            expandImg.setRotation(0);
        }
        else{
            listView.setOrientation(TwoWayView.Orientation.VERTICAL);
            listViewServers.setOrientation(TwoWayView.Orientation.HORIZONTAL);
            expandImg.setRotation(90);
        }

        if(savedInstanceState != null)
        {
            if(savedInstanceState.getBoolean(keyRestoreSidebarOpen, false))
            {
                toggleSidebar();
            }
        }
        //RepopulateVolumeDataListViewAdapter(new VolumeData[]{new VolumeData("Master", .95f, false, -1, "0"), new VolumeData("System", .65f, false, 0, "1"), new VolumeData("Firefox", .95f, false, 1, "2"), new VolumeData("Spotify", .30f, false, 3, "3"), new VolumeData("Steam", .10f, false, 5, "66")});
        //RefreshServerListValues(new VolumeServer[]{new VolumeServer(false, "Mika-PC", "192.168.0.122"), new VolumeServer(true, "SERVER", "192.168.0.117", "123"), new VolumeServer(false, "Work-PC", "192.168.0.217", "3rt4"), new VolumeServer(true, "SERVER1", "192.168.0.118"), new VolumeServer(true, "SERVER2", "192.168.0.119")});
    }

    @Override
    protected void onSaveInstanceState (Bundle outState)
    {
        outState.putBoolean(keyRestoreSidebarOpen, sideBarExpanded);
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
                llConnectionTip.setVisibility(View.GONE);
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
        dialog.setContentView(R.layout.alert_dialog_first_use_layout);
        dialog.show();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(ShowInstallInstructionsOnStart_PrefKey, false);
        editor.apply();
    }



    public void setNightMode(boolean nightMode)
    {
        LinearLayout llSidebar = (LinearLayout)findViewById(R.id.sideBarContentLL);
        FrameLayout serverCount = (FrameLayout)findViewById(R.id.serverCountWrapper);
        LinearLayout llExpandSidebar = (LinearLayout)findViewById(R.id.llExpandSidebar);
        ImageView imgPullDown = (ImageView)findViewById(R.id.imgPullDown);
        ImageView imgBtnPopupMenu = (ImageView)findViewById(R.id.imgBtnPopupMenuMain);
        ImageView expandImg = (ImageView) findViewById(R.id.expandImg);
        TextView tvPullDown = (TextView)findViewById(R.id.tvPullToRefresh);
        TextView tvServerCount = (TextView)findViewById(R.id.tvServerCount);
        TwoWayView twoWayViewSliders =(TwoWayView)findViewById(R.id.lvItems);

        if(nightmode) {
            master_icon_res_id = R.mipmap.audio_nightmode_icon;
            mute_icon_res_id = R.mipmap.audio_mute_nightmode_icon;
            popup_menu_icon_res_id = R.mipmap.popup_menu_nightmode_icon;
            system_icon_res_id = R.mipmap.server_nightmode_icon;
            pull_down_icon_res_id = R.mipmap.swipe_down_nightmode_icon;
            application_icon_res_id = R.mipmap.application_nightmode_icon;
            expand_right_icon_res_id = R.mipmap.expand_right_nightmode_icon;
            seekbar_progress_drawable = getResources().getDrawable(R.drawable.seekbar_progressbar_nightmode);


            twoWayViewSliders.setBackgroundResource(R.color.colorBackgroundNightLight);
            llSidebar.setBackgroundResource(R.color.colorBackgroundNightLight);
            serverCount.setBackgroundResource(R.color.colorBackgroundSecondaryNight);
            tvServerCount.setTextColor(ContextCompat.getColor(MainActivity.Instance, R.color.colorTextNight));
            tvPullDown.setTextColor(ContextCompat.getColor(MainActivity.Instance, R.color.colorTextNight));
            llExpandSidebar.setBackgroundResource(R.color.colorExpandSidebarButtonNight);
            listViewServers.setBackgroundResource(R.color.colorBackgroundNightLight);
        }
        else{
            master_icon_res_id = R.mipmap.audio_icon;
            mute_icon_res_id = R.mipmap.audio_mute_icon;
            popup_menu_icon_res_id = R.mipmap.popup_menu_icon;
            system_icon_res_id = R.mipmap.server_icon;
            pull_down_icon_res_id = R.mipmap.swipe_down_grey_icon;
            application_icon_res_id = R.mipmap.application_icon;
            expand_right_icon_res_id = R.mipmap.expand_right_icon;
            seekbar_progress_drawable = getResources().getDrawable(R.drawable.seekbar_progressbar);

            serverCount.setBackgroundResource(R.color.colorBackgroundSecondary);
            tvPullDown.setTextColor(ContextCompat.getColor(MainActivity.Instance, R.color.colorText));
            tvServerCount.setTextColor(ContextCompat.getColor(MainActivity.Instance, R.color.colorText));
            llSidebar.setBackgroundResource(R.color.colorBackgroundSidebar);
            llExpandSidebar.setBackgroundResource(R.color.colorExpandSidebarButton);
            twoWayViewSliders.setBackgroundResource(R.color.colorBackgroundSecondary);
            listViewServers.setBackgroundResource(R.color.colorBackgroundSecondary);
        }

        imgPullDown.setImageResource(pull_down_icon_res_id);
        imgBtnPopupMenu.setImageResource(popup_menu_icon_res_id);
        expandImg.setImageResource(expand_right_icon_res_id);

        adapter.refreshProgressDrawables = true;
        adapter.notifyDataSetChanged();
        serverListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (useFullscreen && Build.VERSION.SDK_INT >= 19) {
            hideSystemUI(getWindow().getDecorView());
        }
    }

            // This snippet hides the system bars.
    @TargetApi(19)
    private void hideSystemUI(View decorView) {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        decorView.setSystemUiVisibility(

                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    @TargetApi(19)
    public void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.VISIBLE);
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
                            editor.putString(ServerStandardPasswordPrefix_PrefKey + VCCryptography.getMD5Hash(serverListViewAdapter.listElements.get(pos).RSAPublicKey), "");
                            serverListViewAdapter.listElements.get(pos).standardPassword = "";
                            editor.apply();

                            Log.i(TAG, "Forgot password for " + serverListViewAdapter.listElements.get(pos).name);
                        } else if (item.getTitle().equals(getString(R.string.server_menu_disconnect))) {
                            MainActivity.Instance.serverListViewAdapter.removeActive();
                            MainActivity.Instance.adapter.clear();

                            SharedPreferences.Editor editor = MainActivity.Instance.getPreferences(MainActivity.MODE_PRIVATE).edit();
                            editor.putString(MainActivity.LastConnectedServer_PrefKey, null);
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
                    Log.i(TAG, "Saved Password:" + prefs.getString(ServerStandardPasswordPrefix_PrefKey + VCCryptography.getMD5Hash(selectedServer.RSAPublicKey), ""));
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
                            prefEditor.putString(ServerStandardPasswordPrefix_PrefKey + VCCryptography.getMD5Hash(selectedServer.RSAPublicKey), selectedServer.standardPassword);
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

    public void initializeServerRefresh(){
        if(Build.VERSION.SDK_INT >= 19 && !useAlternativeServerRefresh)
        {
            refreshServersTip = findViewById(R.id.llPullToRefresh);

            ((ImageView)findViewById(R.id.imgPullDown)).setImageResource(R.mipmap.swipe_down_light_grey_icon);

            swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

            swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);

            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new NetworkDiscoveryThread().start();
                }
            });

            //Show refreshing Icon in servers ListView while initially looking for Servers
            swipeContainer.post(new Runnable() {
                @Override
                public void run() {
                    swipeContainer.setRefreshing(true);
                }
            });

        }
        else
        {
            refreshServersTip = findViewById(R.id.btnResearchServers);

            if(useAlternativeServerRefresh)
            {
               SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeContainer);
                swipeRefreshLayout.setEnabled(false);
                swipeRefreshLayout.setRefreshing(false);
            }

            refreshServersTip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new NetworkDiscoveryThread().start();
                    refreshServersTip.setEnabled(false);
                }
            });
        }
    }

    public void initializeSidebar(){
        final ImageButton btnExpand = (ImageButton) findViewById(R.id.btnExpand);
        final LinearLayout sideBarContentLL = (LinearLayout) findViewById(R.id.sideBarContentLL);
        final ImageButton btnPupupMenu = (ImageButton) findViewById(R.id.imgBtnPopupMenuMain);
        btnPupupMenu.setFocusable(false);

        final GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if(sideBarExpanded)
                {
                    if(e1.getX() < e2.getX())
                    {
                        toggleSidebar();
                    }
                }
                else{
                        toggleSidebar();
                }
                return true;
            }
        };

        final GestureDetector detector = new GestureDetector(this, gestureListener);

        btnExpand.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                detector.onTouchEvent(e);
                return false;
            }
        });



        btnPupupMenu.setImageResource(R.mipmap.popup_menu_icon);
        btnPupupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.Instance, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.overflow_menu_main, popup.getMenu());



                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String title = item.getTitle().toString();

                        if(title.equals(getString(R.string.main_menu_refresh)))
                        {
                            new NetworkDiscoveryThread().start();
                        }
                        else if(title.equals(getString(R.string.main_menu_how_to_use)))
                        {
                            showInstructionsDialog();
                        }
                        else if(title.equals(getString(R.string.main_menu_settings)))
                        {
                            Intent settingsIntent = new Intent(MainActivity.Instance, SettingsActivity.class);

                            startActivity(settingsIntent);
                        }
                        else if(title.equals(getString(R.string.main_menu_feedback)))
                        {
                            //Intent feedbackIntent = new Intent(MainActivity.Instance, feedback.class);
                            //startActivity(feedbackIntent);
                        }

                        return true;
                    }
                });
            }
        });

        btnExpand.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                    toggleSidebar();
            }
        });
    }

    public void toggleSidebar(){
        final ImageButton btnExpand = (ImageButton) findViewById(R.id.btnExpand);
        final ImageView expandImg = (ImageView) findViewById(R.id.expandImg);
        final LinearLayout sideBarContentLL = (LinearLayout) findViewById(R.id.sideBarContentLL);

        if(isLandscape) {
            if (sideBarExpanded) {
                //   btnExpand.setImageResource(R.mipmap.expand_right_icon);

                ValueAnimator rotate = ValueAnimator.ofFloat(expandImg.getRotation(), 0);
                Easing easing = new Easing(250);
                rotate.setEvaluator(easing);
                rotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float rotVal = (float) valueAnimator.getAnimatedValue();
                        expandImg.setRotation(rotVal);
                    }
                });
                rotate.setDuration(250);
                rotate.start();

                ValueAnimator anim = ValueAnimator.ofInt(sideBarContentLL.getMeasuredWidth(), 0);

                anim.setEvaluator(easing);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = sideBarContentLL.getLayoutParams();
                        layoutParams.width = val;
                        sideBarContentLL.setLayoutParams(layoutParams);
                    }
                });
                anim.setDuration(250);
                anim.start();

                //  sideBarContentLL.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
                sideBarExpanded = false;
            } else {
                //   btnExpand.setImageResource(R.mipmap.collapse_left_icon);

                ValueAnimator rotate = ValueAnimator.ofFloat(expandImg.getRotation(), 180);
                Easing easing = new Easing(250);
                rotate.setEvaluator(easing);
                rotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float rotVal = (float) valueAnimator.getAnimatedValue();
                        expandImg.setRotation(rotVal);
                    }
                });
                rotate.setDuration(250);
                rotate.start();

                ValueAnimator anim = ValueAnimator.ofInt(sideBarContentLL.getMeasuredWidth(), getWindowManager().getDefaultDisplay().getWidth() / 5);
                anim.setEvaluator(easing);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = sideBarContentLL.getLayoutParams();
                        layoutParams.width = val;
                        sideBarContentLL.setLayoutParams(layoutParams);
                    }
                });
                anim.setDuration(250);
                anim.start();

                //    sideBarContentLL.setLayoutParams(new LinearLayout.LayoutParams(120, ViewGroup.LayoutParams.MATCH_PARENT));
                sideBarExpanded = true;
            }
        }
        else
        {
            if (sideBarExpanded) {
                //   btnExpand.setImageResource(R.mipmap.expand_right_icon);

                ValueAnimator rotate = ValueAnimator.ofFloat(expandImg.getRotation(), 90);
                Easing easing = new Easing(250);
                rotate.setEvaluator(easing);
                rotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float rotVal = (float) valueAnimator.getAnimatedValue();
                        expandImg.setRotation(rotVal);
                    }
                });
                rotate.setDuration(250);
                rotate.start();

                ValueAnimator anim = ValueAnimator.ofInt(sideBarContentLL.getMeasuredHeight(), 0);

                anim.setEvaluator(easing);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = sideBarContentLL.getLayoutParams();
                        layoutParams.height = val;
                        sideBarContentLL.setLayoutParams(layoutParams);
                    }
                });
                anim.setDuration(250);
                anim.start();

                //  sideBarContentLL.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
                sideBarExpanded = false;
            } else {
                //   btnExpand.setImageResource(R.mipmap.collapse_left_icon);

                ValueAnimator rotate = ValueAnimator.ofFloat(expandImg.getRotation(), 270);
                Easing easing = new Easing(250);
                rotate.setEvaluator(easing);
                rotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float rotVal = (float) valueAnimator.getAnimatedValue();
                        expandImg.setRotation(rotVal);
                    }
                });
                rotate.setDuration(250);
                rotate.start();

                ValueAnimator anim = ValueAnimator.ofInt(sideBarContentLL.getMeasuredHeight(), getWindowManager().getDefaultDisplay().getHeight() / 6);
                anim.setEvaluator(easing);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = sideBarContentLL.getLayoutParams();
                        layoutParams.height = val;
                        sideBarContentLL.setLayoutParams(layoutParams);
                    }
                });
                anim.setDuration(250);
                anim.start();

                //    sideBarContentLL.setLayoutParams(new LinearLayout.LayoutParams(120, ViewGroup.LayoutParams.MATCH_PARENT));
                sideBarExpanded = true;
            }
        }
    }

    public void RefreshServerListValues(VolumeServer[] data)
    {
        if(data != null)
        {
            serverListViewAdapter.clear();
            for(int i = 0; i < data.length; i++){
                serverListViewAdapter.add(data[i]);
            }
        }
    }

    public void RepopulateVolumeDataListViewAdapter(VolumeData[] data)
    {
        if(data != null)
        {
            adapter.clear();
            for(int i = 0; i < data.length; i++)
            {
                adapter.add(data[i]);
            }
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

    public int getScreenOrientation()
    {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if(getOrient.getWidth()==getOrient.getHeight()){
            orientation = Configuration.ORIENTATION_SQUARE;
        } else{
            if(getOrient.getWidth() < getOrient.getHeight()){
                orientation = Configuration.ORIENTATION_PORTRAIT;
            }else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }
}

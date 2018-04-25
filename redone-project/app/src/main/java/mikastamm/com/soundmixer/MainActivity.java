package mikastamm.com.soundmixer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.Networking.NetworkDiscoveryBroadcastSender;
import mikastamm.com.soundmixer.Networking.ServerConnectionList;
import mikastamm.com.soundmixer.Networking.ServerLogic;
import mikastamm.com.soundmixer.UI.GettingStartedFragment;
import mikastamm.com.soundmixer.UI.NavigationViewPresenter;
import mikastamm.com.soundmixer.UI.NoVolumeSlidersToShowReplacer;
import mikastamm.com.soundmixer.UI.ServerPresenter;
import mikastamm.com.soundmixer.UI.SettingsFragment;
import mikastamm.com.soundmixer.UI.VolumeSlidersFragment;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "sound-mixer-log";

    private NavigationViewPresenter navigationViewPresenter;
    private ServerPresenter serverPresenter;

    //Sends broadcast and receives response from all available servers in the network
    public NetworkDiscoveryBroadcastSender ndSender;

    private NoVolumeSlidersToShowReplacer nothingToShow = new NoVolumeSlidersToShowReplacer(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ndSender = new NetworkDiscoveryBroadcastSender(this);

        GettingStartedFragment fragment = new GettingStartedFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fgGettingStarted, fragment).commit();

        navigationViewPresenter = new NavigationViewPresenter(this);
        serverPresenter = new ServerPresenter(this, navigationViewPresenter.navigationView);

        nothingToShow.startListening();
        nothingToShow.showReplacer();

        ndSender.searchForServers();
    }

    @Override
    protected void onStop(){
        super.onStop();
        ndSender.stopSearch();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        nothingToShow.stopListening();
        serverPresenter.dispose();

        if(isFinishing())
        ServerConnectionList.getInstance().disconnectAndClear();
    }

    public void connect(final Server s)
    {
        if(s != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setDisplayedFragment(VolumeSlidersFragment.newInstanceAndConnect(s), s.name);
                }
            });
        }
        else
            Log.e(MainActivity.TAG, "MainActivity.connect(Server): Did not connect: passed Server was null");
    }

    public void setDisplayedFragment(Fragment fragment, String newTitle)
    {
        // Insert the fragment by replacing any existing fragment
        if(fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction tx = fragmentManager.beginTransaction().replace(R.id.flContent, fragment);

            tx.commitAllowingStateLoss();
            setTitle(newTitle);
        }
    }

    public void setDisplayedFragment(Class<? extends Fragment> fragmentClass, String newTitle)
    {
        Fragment fragment = null;

        try {
            fragment = fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setDisplayedFragment(fragment, newTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstance)
    {
        super.onPostCreate(savedInstance);
        navigationViewPresenter.actionBarToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change (e.g. orientation change) to the drawer toggles
        navigationViewPresenter.actionBarToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return navigationViewPresenter.actionBarToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }


}

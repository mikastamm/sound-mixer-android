package mikastamm.com.soundmixer;

import android.content.res.Configuration;
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
import android.view.MenuItem;

import mikastamm.com.soundmixer.Networking.NetworkDiscoveryBroadcastSender;
import mikastamm.com.soundmixer.Networking.ServerLogic;
import mikastamm.com.soundmixer.UI.NavigationViewPresenter;
import mikastamm.com.soundmixer.UI.ServerPresenter;
import mikastamm.com.soundmixer.UI.SettingsFragment;
import mikastamm.com.soundmixer.UI.VolumeSlidersFragment;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "sound-mixer-log";

    private NavigationViewPresenter navigationViewPresenter;
    private ServerPresenter serverPresenter;

    //Sends broadcast and receives response from all available servers in the network
    public NetworkDiscoveryBroadcastSender ndSender;
    //Provides a handle for the active server to send, receive and handle data
    //the active server is the one that's currently displayed one the main screen (VolumeSlidersFragment)
    public ServerLogic activeServerLogic;

    private final Class<? extends Fragment> homeFragment = VolumeSlidersFragment.class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDisplayedFragment(homeFragment, getString(R.string.app_name));

        navigationViewPresenter = new NavigationViewPresenter(this);
        serverPresenter = new ServerPresenter(this, navigationViewPresenter.navigationView);

        //Networking
        ndSender = new NetworkDiscoveryBroadcastSender(this);
        ndSender.searchForServers();
    }

    @Override
    protected void onStop(){
        super.onStop();
        serverPresenter.dispose();
        ndSender.stopSearch();
    }

    public void setDisplayedFragment(Class<? extends Fragment> fragmentClass, String newTitle)
    {
        Fragment fragment = null;
        try {
            fragment = fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        if(fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction tx = fragmentManager.beginTransaction().replace(R.id.flContent, fragment);

            if(!fragmentClass.equals(homeFragment))
                tx.addToBackStack(fragmentClass.getSimpleName());

            tx.commit();
            setTitle(newTitle);
        }
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

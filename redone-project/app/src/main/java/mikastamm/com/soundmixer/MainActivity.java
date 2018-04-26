package mikastamm.com.soundmixer;

import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.Networking.NetworkDiscoveryBroadcastSender;
import mikastamm.com.soundmixer.Networking.ServerConnectionList;
import mikastamm.com.soundmixer.UI.NavigationViewSetup;
import mikastamm.com.soundmixer.UI.NoVolumeSlidersToShowPlaceholder;
import mikastamm.com.soundmixer.UI.ServerPresenter;
import mikastamm.com.soundmixer.UI.VolumeSlidersFragment;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "sound-mixer-log";

    //Sets up and manages the navigation drawer (Sidebar)
    private NavigationViewSetup navigationViewSetup;

    //Keeps the Servers in the Sidebar up to date with the Datamodel
    private ServerPresenter serverPresenter;

    //Sends broadcast and receives response from all available servers in the network
    public NetworkDiscoveryBroadcastSender networkDiscoveryBroadcastSender = new NetworkDiscoveryBroadcastSender(this);

    //Placeholder for when not connected to any server
    private NoVolumeSlidersToShowPlaceholder nothingToShowPlaceholder = new NoVolumeSlidersToShowPlaceholder(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigationViewSetup = new NavigationViewSetup(this);
        serverPresenter = new ServerPresenter(this, navigationViewSetup.navigationView);

        if (!fragmentRetained()) {
            nothingToShowPlaceholder.startListening();
            nothingToShowPlaceholder.showReplacer();
        }

        networkDiscoveryBroadcastSender.searchForServers();
    }

    private boolean fragmentRetained() {
        return getSupportFragmentManager().findFragmentByTag(VolumeSlidersFragment.class.getSimpleName()) == null;
    }

    public void connect(final Server s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setDisplayedFragment(VolumeSlidersFragment.newInstanceAndConnect(s), s.name);
            }
        });
    }

    public void setDisplayedFragment(Fragment fragment, String newTitle) {
        // Insert the fragment by replacing any existing fragment
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction tx = fragmentManager.beginTransaction().replace(R.id.flContent, fragment, fragment.getClass().getSimpleName());

            tx.commitAllowingStateLoss();
            setTitle(newTitle);
        }
    }

    public void setDisplayedFragment(Class<? extends Fragment> fragmentClass, String newTitle) {
        Fragment fragment = null;

        try {
            fragment = fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setDisplayedFragment(fragment, newTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstance) {
        super.onPostCreate(savedInstance);
        navigationViewSetup.actionBarToggle.syncState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        networkDiscoveryBroadcastSender.stopSearch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        nothingToShowPlaceholder.stopListening();
        serverPresenter.dispose();

        if (isFinishing())
            ServerConnectionList.getInstance().disconnectAndClear();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change (e.g. orientation change) to the drawer toggles
        navigationViewSetup.actionBarToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return navigationViewSetup.actionBarToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }


}

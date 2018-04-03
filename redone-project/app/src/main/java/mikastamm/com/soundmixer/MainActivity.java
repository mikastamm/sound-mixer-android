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

import mikastamm.com.soundmixer.UI.SettingsFragment;
import mikastamm.com.soundmixer.UI.VolumeSlidersFragment;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle actionBarToggle;
    private NavigationView nvDrawer;

    private final Class<? extends Fragment> homeFragment = VolumeSlidersFragment.class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDisplayedFragment(homeFragment, getString(R.string.app_name));

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(actionBarToggle);

        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);
    }



    public void selectDrawerItem(MenuItem menuItem) {

        switch(menuItem.getItemId()) {
            case R.id.nav_refresh:
                //TODO: Implement Refresh
                break;
            case R.id.nav_info:
                //TODO: Implement Info
                break;
            case R.id.nav_settings:
                setDisplayedFragment(SettingsFragment.class, getString(R.string.title_activity_settings));
                break;
            case R.id.nav_feedback:
                //TODO: Implement Feedback
                break;
        }

        mDrawer.closeDrawers();
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
        actionBarToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        actionBarToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }
}

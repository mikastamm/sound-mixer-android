package mikastamm.com.soundmixer.UI;

import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.ServerLogic;
import mikastamm.com.soundmixer.R;
import mikastamm.com.soundmixer.ServerList;

/**
 * Created by Mika on 09.04.2018.
 */

public class NavigationViewPresenter {
    private MainActivity mainActivity;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarToggle;
    public NavigationView navigationView;

    public NavigationViewPresenter(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        setupNavigationDrawer();
    }

    private void setupNavigationDrawer(){
        // Set a Toolbar to replace the ActionBar.
        toolbar = mainActivity.findViewById(R.id.toolbar);
        mainActivity.setSupportActionBar(toolbar);

        drawerLayout = mainActivity.findViewById(R.id.drawer_layout);
        actionBarToggle = setupDrawerToggle();
        drawerLayout.addDrawerListener(actionBarToggle);

        // Find our drawer view
        navigationView = mainActivity.findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(navigationView);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(mainActivity, drawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectClickedDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    private void selectClickedDrawerItem(MenuItem menuItem) {
        //Find the clicked DrawerItem and react accordingly
        switch(menuItem.getItemId()) {
            case R.id.nav_refresh:
                mainActivity.ndSender.searchForServers();
                break;
            case R.id.nav_info:
                drawerLayout.closeDrawers();
                toggleGettingStartedOverlay();
                break;
            case R.id.nav_settings:
                mainActivity.setDisplayedFragment(SettingsFragment.class, mainActivity.getString(R.string.title_activity_settings));
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_feedback:
                //TODO: Implement Feedback
                break;
            default:
                selectClickedServer(menuItem);
                drawerLayout.closeDrawers();
        }


    }

    private void toggleGettingStartedOverlay(){
        final int showAfter = 200;
        final GettingStartedFragment fragment = (GettingStartedFragment)mainActivity.getSupportFragmentManager().findFragmentById(R.id.fgGettingStarted);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(showAfter);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragment.toggle();
                    }
                });
            }
        }).start();
    }

    private void selectClickedServer(MenuItem menuItem){
        for(Server s : ServerList.getInstance())
        {
            if(menuItem.getItemId() == s.getIntegerServerId())
            {
                mainActivity.connect(s);
            }
        }
    }


}

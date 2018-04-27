package mikastamm.com.soundmixer.UI;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.ServerList;
import mikastamm.com.soundmixer.ServerStateChangeDelegate;

/**
 * Created by Mika on 25.04.2018.
 */

public class NoVolumeSlidersToShowPlaceholder {
    private MainActivity mainActivity;
    private ServerStateChangeDelegate.ServerStateChangeListener stateChangeListener = new ServerStateChangeDelegate.ServerStateChangeListener() {

        @Override
        public void onActiveServerChanged(Server oldActive, Server newActive) {
            //if there isnt an active server display the placeholder
            if(newActive == null)
            {
                showReplacer();
            }
        }

        @Override public void onServerConnected(Server server) {}
        @Override public void onServerDisconnected(Server server) {}
    };

    public NoVolumeSlidersToShowPlaceholder(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
    }

    public void startListening(){
        ServerList.getInstance().stateChangeDelegate.addServerStateChangeListener(stateChangeListener);
    }

    public void stopListening(){
        ServerList.getInstance().stateChangeDelegate.removeServerStateChangeListener(stateChangeListener);
    }

    public void showReplacer(){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.setDisplayedFragment(NothingToShowFragment.class, "Disconnected");
            }
        });
    }
}

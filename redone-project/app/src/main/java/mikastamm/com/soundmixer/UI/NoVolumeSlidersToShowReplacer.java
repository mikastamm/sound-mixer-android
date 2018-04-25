package mikastamm.com.soundmixer.UI;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.ServerList;
import mikastamm.com.soundmixer.ServerListeners;

/**
 * Created by Mika on 25.04.2018.
 */

public class NoVolumeSlidersToShowReplacer {
    private MainActivity mainActivity;
    private ServerListeners.ServerStateChangeListener stateChangeListener = new ServerListeners.ServerStateChangeListener() {

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

    public NoVolumeSlidersToShowReplacer(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
    }

    public void startListening(){
        ServerList.getInstance().listeners.addServerStateChangeListener(stateChangeListener);
    }

    public void stopListening(){
        ServerList.getInstance().listeners.removeServerStateChangeListener(stateChangeListener);
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

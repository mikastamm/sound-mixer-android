package mikastamm.com.soundmixer.Networking;

import android.app.Activity;

/**
 * Created by Mika on 03.04.2018.
 */

public class NetworkDiscoveryBroadcastSender {
    private Activity activity;
    private FindServersRunnable findServersRunnable;

    public NetworkDiscoveryBroadcastSender(Activity activity){
        this.activity = activity;
    }

    public void searchForServers()
    {
        findServersRunnable = new FindServersRunnable(activity);
        new Thread(findServersRunnable).start();
    }

    public void stopSearch(){
        if(findServersRunnable != null)
            findServersRunnable.stopReceiving();
    }
}

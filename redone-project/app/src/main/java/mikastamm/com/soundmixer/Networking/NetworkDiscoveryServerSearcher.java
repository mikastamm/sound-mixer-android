package mikastamm.com.soundmixer.Networking;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mika on 03.04.2018.
 */

public class NetworkDiscoveryServerSearcher {
    private Activity activity;
    private FindServersRunnable findServersRunnable;
    //The delegate stores and calls the subscribed NetworkDiscoveryListeners
    public NetworkDiscoveryDelegate delegate = new NetworkDiscoveryDelegate();

    public NetworkDiscoveryServerSearcher(Activity activity){
        this.activity = activity;
    }

    public void searchForServers()
    {
        findServersRunnable = new FindServersRunnable(activity, delegate);
        new Thread(findServersRunnable).start();
        delegate.networkDiscoveryStarted();
    }

    public void stopSearch(){
        if(findServersRunnable != null)
            findServersRunnable.stopReceiving();
    }

    public static class NetworkDiscoveryDelegate{
        public interface NetworkDiscoveryListener{
            void onNetworkDiscoveryStarted();
            void onNetworkDiscoveryFinished();
        }
        private List<NetworkDiscoveryListener> listeners = new ArrayList<>();
        public void networkDiscoveryStarted(){
            for(NetworkDiscoveryListener l : listeners)
            {
                l.onNetworkDiscoveryStarted();
            }
        }

        public void networkDiscoveryFinished(){
            for(NetworkDiscoveryListener l : listeners)
            {
                l.onNetworkDiscoveryFinished();
            }
        }

        public void addListener(NetworkDiscoveryListener listener)
        {
            listeners.add(listener);
        }

        public void removeListener(NetworkDiscoveryListener listener)
        {
            listeners.remove(listener);
        }
    }
}

package mikastamm.com.soundmixer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Mika on 27.03.2018.
 */

public class KnownServerList {
    private static String KnownServersPrefKey = "knownServers";
    private static String LastConnectedServerPrefKey = "lastConnected";

    public static void setLastConnectedServer(String serverId, Activity activity)
    {
        SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(LastConnectedServerPrefKey, serverId);
        edit.apply();
    }

    public static boolean isKnown(String RSAKey, Activity activity)
    {
        SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
        Set<String> knownServers = prefs.getStringSet(KnownServersPrefKey, new HashSet<String>());

        return knownServers.contains(RSAKey);
    }

    public static void addToKnown(String RSAKey, Activity activity)
    {
        SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        Set<String> knownServers = prefs.getStringSet(KnownServersPrefKey, new HashSet<String>());
        if(!knownServers.contains(RSAKey))
            knownServers.add(RSAKey);

        edit.putStringSet(KnownServersPrefKey, knownServers);
        edit.apply();
    }

    public static void forget(String RSAKey, Activity activity)
    {
        SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        Set<String> knownServers = prefs.getStringSet(KnownServersPrefKey, new HashSet<String>());
        if(knownServers.contains(RSAKey))
            knownServers.remove(RSAKey);

        edit.putStringSet(KnownServersPrefKey, knownServers);
        edit.apply();
    }
}

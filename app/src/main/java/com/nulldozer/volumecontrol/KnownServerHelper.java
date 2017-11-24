package com.nulldozer.volumecontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Display;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Mika on 06.11.2017.
 */

public class KnownServerHelper {
    public static boolean isKnown(String RSAKey, MainActivity mainActivity)
    {
        SharedPreferences prefs = mainActivity.getPreferences(Context.MODE_PRIVATE);
        Set<String> knownServers = prefs.getStringSet(PrefKeys.KnownServers, new HashSet<String>());

        if(knownServers.contains(RSAKey))
            return true;
        else
            return false;
    }

    public static void addToKnown(String RSAKey, MainActivity mainActivity)
    {
        SharedPreferences prefs = mainActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        Set<String> knownServers = prefs.getStringSet(PrefKeys.KnownServers, new HashSet<String>());
        if(!knownServers.contains(RSAKey))
            knownServers.add(RSAKey);

        edit.putStringSet(PrefKeys.KnownServers, knownServers);
        edit.apply();
    }

    public static void forget(String RSAKey, MainActivity mainActivity)
    {
        SharedPreferences prefs = mainActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        Set<String> knownServers = prefs.getStringSet(PrefKeys.KnownServers, new HashSet<String>());
        if(knownServers.contains(RSAKey))
            knownServers.remove(RSAKey);

        edit.putStringSet(PrefKeys.KnownServers, knownServers);
        edit.apply();
    }
}

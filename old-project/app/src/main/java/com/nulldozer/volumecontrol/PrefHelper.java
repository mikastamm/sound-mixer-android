package com.nulldozer.volumecontrol;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Mika on 26.12.2017.
 */

public class PrefHelper {
    public static String getStringPreference(Activity activity, String Key, String defaultValue)
    {
        SharedPreferences prefs = activity.getPreferences(Activity.MODE_PRIVATE);
        return prefs.getString(Key, defaultValue);
    }

    public static void setStringPreference(Activity activity, String Key, String Value)
    {
        SharedPreferences.Editor edit = activity.getPreferences(Activity.MODE_PRIVATE).edit();
        edit.putString(Key, Value);
        edit.apply();
    }
}

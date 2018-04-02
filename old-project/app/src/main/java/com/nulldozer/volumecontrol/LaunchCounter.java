package com.nulldozer.volumecontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Mika on 02.11.2017.
 */

public class LaunchCounter {

    public static void launched(AppCompatActivity activity){
        SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PrefKeys.LaunchCounter, prefs.getInt(PrefKeys.LaunchCounter, 0)+1);
        editor.apply();
    }

    public static int getLaunches(AppCompatActivity activity){
        SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
        return prefs.getInt(PrefKeys.LaunchCounter, 0);
    }
}

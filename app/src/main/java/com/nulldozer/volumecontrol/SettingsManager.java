package com.nulldozer.volumecontrol;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Map;

/**
 * Created by Mika on 15.09.2017.
 */
public class SettingsManager {
    SharedPreferences preferences;
    private static final String TAG = "PreferenceManager";
    public SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.i(TAG, "OnPreferenceChanged: " + key);
            if(key.equals("pref_key_show_ip"))
            {
                MainActivity.showServerIpInServerBrowser = sharedPreferences.getBoolean(key, false);
                Log.i(TAG, "ShowServerIPs: " + MainActivity.showServerIpInServerBrowser);
                MainActivity.Instance.serverListViewAdapter.notifyDataSetChanged();
            }
            else if(key.equals("pref_key_fullscreen"))
            {
                MainActivity.useFullscreen = sharedPreferences.getBoolean(key, false);
                if(!MainActivity.useFullscreen)
                {
                    MainActivity.Instance.showSystemUI();
                }
            }
            else if(key.equals("pref_key_hide_application_icon"))
            {
                MainActivity.hideApplicationIcons = preferences.getBoolean(key, false);
                MainActivity.Instance.adapter.notifyDataSetChanged();
            }
            else if(key.equals("pref_key_nightmode"))
            {
                MainActivity.nightmode = preferences.getBoolean(key, false);
                MainActivity.Instance.setNightMode(MainActivity.nightmode);
            }
            else if(key.equals("pref_key_auto_connect_last"))
            {
                MainActivity.autoConnectToLastConnectedServer = preferences.getBoolean(key, true);
            }
            else if(key.equals("pref_key_auto_connect_open"))
            {
                MainActivity.autoConnectToServersWithoutPassword = preferences.getBoolean(key, false);
            }
            else if(key.equals("pref_key_orientation"))
            {
                String val = preferences.getString(key, "Auto");

                if(val.equals("Auto"))
                {
                    MainActivity.appOrientation = MainActivity.Orientation.AUTO;
                }
                else if(val.equals("Portrait"))
                {
                    MainActivity.appOrientation = MainActivity.Orientation.PORTRAIT;
                }
                else if(val.equals("Landscape"))
                {
                    MainActivity.appOrientation = MainActivity.Orientation.LANDSCAPE;
                }

                MainActivity.Instance.recreate();
            }
        }
    };


    public SettingsManager(){
        preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.Instance);

        Map<String, ?> entries = preferences.getAll();

        for (Map.Entry<String, ?> entry : entries.entrySet())
        {
            String key = entry.getKey();
            if(key.equals("pref_key_show_ip"))
            {
                MainActivity.showServerIpInServerBrowser = preferences.getBoolean(key, false);
                Log.i(TAG, "ShowServerIPs: " + MainActivity.showServerIpInServerBrowser);
                MainActivity.Instance.serverListViewAdapter.notifyDataSetChanged();
                MainActivity.Instance.serverListViewAdapter.notifyDataSetInvalidated();
            }
            else if(key.equals("pref_key_fullscreen"))
            {
                MainActivity.useFullscreen = preferences.getBoolean(key, false);
            }
            else if(key.equals("pref_key_hide_application_icon"))
            {
                MainActivity.hideApplicationIcons = preferences.getBoolean(key, false);
                MainActivity.Instance.adapter.notifyDataSetChanged();
                MainActivity.Instance.serverListViewAdapter.notifyDataSetInvalidated();
            }
            else if(key.equals("pref_key_nightmode"))
            {
                MainActivity.nightmode = preferences.getBoolean(key, false);
                MainActivity.Instance.setNightMode(MainActivity.nightmode);
            }
            else if(key.equals("pref_key_auto_connect_last"))
            {
                MainActivity.autoConnectToLastConnectedServer = preferences.getBoolean(key, true);
            }
            else if(key.equals("pref_key_auto_connect_open"))
            {
                MainActivity.autoConnectToServersWithoutPassword = preferences.getBoolean(key, false);
            }
            else if(key.equals("pref_key_reduce_sensitivity"))
            {
                MainActivity.reduceSliderSensitivity = preferences.getBoolean(key, false);
            }
            else if(key.equals("pref_key_orientation"))
            {
                String val = preferences.getString(key, "Auto");
                Log.i(TAG, "Forced Orientation: " + val);
                if(val.equals("Auto"))
                {
                    MainActivity.appOrientation = MainActivity.Orientation.AUTO;
                }
                else if(val.equals("Portrait"))
                {
                    MainActivity.appOrientation = MainActivity.Orientation.PORTRAIT;
                }
                else if(val.equals("Landscape"))
                {
                    MainActivity.appOrientation = MainActivity.Orientation.LANDSCAPE;
                }
            }
        }

        preferences.registerOnSharedPreferenceChangeListener(listener);
    }
}

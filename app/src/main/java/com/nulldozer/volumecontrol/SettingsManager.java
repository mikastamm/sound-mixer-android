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
                Settings.showServerIpInServerBrowser = sharedPreferences.getBoolean(key, false);
                Log.i(TAG, "ShowServerIPs: " + Settings.showServerIpInServerBrowser);
                MainActivity.Instance.serverListViewAdapter.notifyDataSetChanged();
            }
            else if(key.equals("pref_key_fullscreen"))
            {
                Settings.useFullscreen = sharedPreferences.getBoolean(key, false);
                if(!Settings.useFullscreen)
                {
                    MainActivity.Instance.fullscreen.disable();
                }
            }
            else if(key.equals("pref_key_hide_application_icon"))
            {
                Settings.hideApplicationIcons = preferences.getBoolean(key, false);
                MainActivity.Instance.listViewAdapterVolumeSliders.notifyDataSetChanged();
            }
            else if(key.equals("pref_key_nightmode"))
            {
                Settings.nightmode = preferences.getBoolean(key, false);
                Nightmode.setEnabled(MainActivity.Instance, Settings.nightmode);
            }
            else if(key.equals("pref_key_auto_connect_last"))
            {
                Settings.autoConnectToLastConnectedServer = preferences.getBoolean(key, true);
            }
            else if(key.equals("pref_key_auto_connect_open"))
            {
                Settings.autoConnectToServersWithoutPassword = preferences.getBoolean(key, false);
            }
            else if(key.equals("pref_key_orientation"))
            {
                String val = preferences.getString(key, "Auto");

                if(val.equals("Auto"))
                {
                    Settings.appOrientation = Orientation.AUTO;
                }
                else if(val.equals("Portrait"))
                {
                    Settings.appOrientation = Orientation.PORTRAIT;
                }
                else if(val.equals("Landscape"))
                {
                    Settings.appOrientation = Orientation.LANDSCAPE;
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
                Settings.showServerIpInServerBrowser = preferences.getBoolean(key, false);
                Log.i(TAG, "ShowServerIPs: " + Settings.showServerIpInServerBrowser);
                MainActivity.Instance.serverListViewAdapter.notifyDataSetChanged();
                MainActivity.Instance.serverListViewAdapter.notifyDataSetInvalidated();
            }
            else if(key.equals("pref_key_fullscreen"))
            {
                Settings.useFullscreen = preferences.getBoolean(key, false);
            }
            else if(key.equals("pref_key_hide_application_icon"))
            {
                Settings.hideApplicationIcons = preferences.getBoolean(key, false);
                MainActivity.Instance.listViewAdapterVolumeSliders.notifyDataSetChanged();
                MainActivity.Instance.serverListViewAdapter.notifyDataSetInvalidated();
            }
            else if(key.equals("pref_key_nightmode"))
            {
                Settings.nightmode = preferences.getBoolean(key, false);
            }
            else if(key.equals("pref_key_auto_connect_last"))
            {
                Settings.autoConnectToLastConnectedServer = preferences.getBoolean(key, true);
            }
            else if(key.equals("pref_key_auto_connect_open"))
            {
                Settings.autoConnectToServersWithoutPassword = preferences.getBoolean(key, false);
            }
            else if(key.equals("pref_key_reduce_sensitivity"))
            {
                Settings.reduceSliderSensitivity = preferences.getBoolean(key, false);
            }
            else if(key.equals("pref_key_orientation"))
            {
                String val = preferences.getString(key, "Auto");
                Log.i(TAG, "Forced Orientation: " + val);
                if(val.equals("Auto"))
                {
                    Settings.appOrientation = Orientation.AUTO;
                }
                else if(val.equals("Portrait"))
                {
                    Settings.appOrientation = Orientation.PORTRAIT;
                }
                else if(val.equals("Landscape"))
                {
                    Settings.appOrientation = Orientation.LANDSCAPE;
                }
            }
        }

        preferences.registerOnSharedPreferenceChangeListener(listener);
    }
}

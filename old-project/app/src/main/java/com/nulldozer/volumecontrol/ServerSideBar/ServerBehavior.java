package com.nulldozer.volumecontrol.ServerSideBar;

import android.widget.Toast;

import com.nulldozer.volumecontrol.PrefHelper;
import com.nulldozer.volumecontrol.PrefKeys;
import com.nulldozer.volumecontrol.VCCryptography;

/**
 * Created by Mika on 27.01.2018.
 */

public class ServerBehavior {
    public static void forgetPassword(){
        selected.standardPassword = ""; // "" as standardPassword indicates that no password is currently save for that Server
        PrefHelper.setStringPreference(activity, PrefKeys.ServerStandardPasswordPrefix + VCCryptography.getMD5Hash(selected.RSAPublicKey), selected.standardPassword); //Write change to Preferences

        Toast.makeText(activity, "Forgot password for" + selected.name, Toast.LENGTH_LONG).show();
    }
}

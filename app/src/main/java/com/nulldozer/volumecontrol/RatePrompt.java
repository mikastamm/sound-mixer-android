package com.nulldozer.volumecontrol;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Mika on 02.11.2017.
 */

public class RatePrompt {

    static int showAfterLaunches = 7;

    int launches;
    int prompts;
    MainActivity activity;
    private SharedPreferences prefs;

    public RatePrompt(MainActivity activity)
    {
        this.activity = activity;
        prefs = activity.getPreferences(Context.MODE_PRIVATE);
        prompts =  prefs.getInt(PrefKeys.RatePromptCounter, 0);
    }

    public boolean tryShow(){
        android.support.v4.app.FragmentManager fm = activity.getSupportFragmentManager();
        SharedPreferences.Editor editor = prefs.edit();

        if(prompts == 0 && launches == showAfterLaunches) {
            RateAppDialog rateDialog = new RateAppDialog();
            rateDialog.setMainActivity(activity);
            rateDialog.show(fm, "rate-dialog");
            editor.putInt(PrefKeys.RatePromptCounter, prefs.getInt(PrefKeys.RatePromptCounter, 0)+1);
            editor.apply();
            return true;
        }
        return false;
    }
}

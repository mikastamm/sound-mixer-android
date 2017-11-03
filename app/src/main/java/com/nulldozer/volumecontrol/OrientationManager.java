package com.nulldozer.volumecontrol;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;

/**
 * Created by Mika on 02.11.2017.
 */

public class OrientationManager {

    public boolean isLandscape;

    private AppCompatActivity activity;
    public OrientationManager(AppCompatActivity activity)
    {
        this.activity = activity;
        if(Settings.appOrientation == Orientation.AUTO) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            isLandscape = getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE;
        }
        else if(Settings.appOrientation == Orientation.PORTRAIT)
        {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isLandscape = false;
        }
        else if(Settings.appOrientation == Orientation.LANDSCAPE)
        {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            isLandscape = true;
        }
    }

    private int getScreenOrientation()
    {
        Display getOrient = activity.getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if(getOrient.getWidth()==getOrient.getHeight()){
            orientation = Configuration.ORIENTATION_SQUARE;
        } else{
            if(getOrient.getWidth() < getOrient.getHeight()){
                orientation = Configuration.ORIENTATION_PORTRAIT;
            }else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }
}

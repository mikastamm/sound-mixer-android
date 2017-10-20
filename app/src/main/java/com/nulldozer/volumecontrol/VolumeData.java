package com.nulldozer.volumecontrol;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by Mika on 27.04.2016.
 */
public class VolumeData {
    public String title;
    public float volume;
    public boolean mute;
    public String id;

    public transient boolean isTracking;
    public transient boolean ignoreNextMute;
    public transient boolean sentLast;

    public VolumeData(String name, float volume, boolean mute, String sessionID)
    {
        this.title = name;
        this.volume = volume;
        this.mute = mute;
        this.id = sessionID;
    }

    public VolumeData(){}

    public void print()
    {
        Log.i("VolumeData", "Title=" + title + "\nVolume=" + volume + "\nMute=" + mute + "\nGuid="+id);
    }
}



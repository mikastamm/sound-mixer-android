package mikastamm.com.soundmixer.Datamodel;

import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;

import mikastamm.com.soundmixer.Exceptions.AudioSessionIdMismatchException;

public class AudioSession {
    public String title;
    public float volume;
    public boolean mute;
    public String id;
    public Bitmap icon;

    public transient boolean isTracking;

    public AudioSession(String name, float volume, boolean mute, String sessionID)
    {
        this.title = name;
        this.volume = volume;
        this.mute = mute;
        this.id = sessionID;
    }

    public AudioSession(String name, float volume, boolean mute, String sessionID, Bitmap icon)
    {
        this(name, volume, mute, sessionID);
        this.icon = icon;
    }

    public AudioSession(){}

    public void updateValues(AudioSession session)
    {
        if(id.equals(session.id)) {
            title = session.title;
            volume = session.volume;
            mute = session.mute;
            icon = session.icon;
        }
        else{
            throw new AudioSessionIdMismatchException("Audio Session ID Mismatch : ("+id +" to " + session.id +") Attempted to update Audio Session with another Audio Session that does not have the same id");
        }
    }

    public AudioSession copy(){
        return new AudioSession(title, volume, mute, id, icon);
    }
}

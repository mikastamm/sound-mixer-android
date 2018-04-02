package mikastamm.com.soundmixer.Datamodel;

import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by Mika on 28.03.2018.
 */

public class AudioSession {
    public String title;
    public float volume;
    public boolean mute;
    public String id;
    public Bitmap icon;

    public AudioSession(String name, float volume, boolean mute, String sessionID)
    {
        this.title = name;
        this.volume = volume;
        this.mute = mute;
        this.id = sessionID;
    }

    public AudioSession(){}

    public void updateValues(AudioSession session)
    {
        if(id.equals(session.id)) {
            title = session.title;
            volume = session.volume;
            mute = session.mute;
        }
        else{
            throw new RuntimeException("Audio Session ID Mismatch : ("+id +" to " + session.id +") Attempted to update Audio Session with another Audio Session that does not have the same id");
        }
    }

    public AudioSession copy(){
        AudioSession ret = new AudioSession(title, volume, mute, id);
        ret.icon = icon;
        return ret;
    }
}

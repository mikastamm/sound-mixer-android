package mikastamm.com.soundmixer;

import java.util.HashSet;
import java.util.Set;

import mikastamm.com.soundmixer.Datamodel.AudioSessionIcon;
import mikastamm.com.soundmixer.Datamodel.DecodedAudioSessionIcon;

/**
 * Created by Mika on 11.04.2018.
 */

public class AudioSessionIconList {
    //Instantiation (Singleton)
    private AudioSessionIconList(){}
    private static AudioSessionIconList instance = null;

    public static AudioSessionIconList getInstance()
    {
        if(instance == null)
            instance = new AudioSessionIconList();
        return instance;
    }

    //Logic
    private Set<DecodedAudioSessionIcon> icons = new HashSet<>();
    public void add(DecodedAudioSessionIcon sessionIcon)
    {
        icons.add(sessionIcon);
    }

    public DecodedAudioSessionIcon get(String sessionId)
    {
        DecodedAudioSessionIcon ret = null;
        for(DecodedAudioSessionIcon c : icons)
        {
            if(c.id.equals(sessionId))
                ret = c;
        }
        return ret;
    }

    public boolean contains(String sessionId)
    {
        boolean ret = false;
        for(DecodedAudioSessionIcon c : icons)
        {
            if(c.id.equals(sessionId))
                ret = true;
        }
        return ret;
    }

    public void remove(String sessionId)
    {
        DecodedAudioSessionIcon toDelete = null;
        for(DecodedAudioSessionIcon c : icons)
        {
            if(c.id.equals(sessionId))
            {
                toDelete = c;
            }
        }

        if(toDelete != null) {
            icons.remove(toDelete);
        }
    }

    public void clear() {
        icons.clear();
    }
}

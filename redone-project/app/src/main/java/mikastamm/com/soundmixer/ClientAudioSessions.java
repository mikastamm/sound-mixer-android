package mikastamm.com.soundmixer;

import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Datamodel.AudioSessionIcon;

public class ClientAudioSessions {
    public String ownerClientId;
    public AudioSessionListener listener;

    private List<AudioSession> audioSessions = new ArrayList<>();

    public ClientAudioSessions(){
        listener = new AudioSessionListener();
    }

    public void addAudioSession(AudioSession session)
    {
        audioSessions.add(session);
        listener.onAudioSessionAdded(session);
    }

    public void addAudioSessions(AudioSession[] sessions)
    {
        for(AudioSession session : sessions) {
            audioSessions.add(session);
            listener.onAudioSessionAdded(session);
        }
    }

    public void editAudioSession(AudioSession newValues)
    {
        AudioSession oldValues;

        for(AudioSession s : audioSessions)
        {
            if(s.id.equals(newValues.id))
            {
                oldValues = s.copy();
                s.updateValues(newValues);

                listener.onAudioSessionEdited(oldValues, s);
                break;
            }
        }
    }

    public void setAudioSessionImage(AudioSessionIcon img)
    {
        AudioSession s = getAudioSession(img.id).copy();
        s.icon = ImageEncodingFactory.getStandardEncoding().decode(img.icon);
        editAudioSession(s);
    }

    public void removeAudioSession(String id){
        AudioSession del = null;
        for(AudioSession s : audioSessions)
        {
            if(s.id.equals(id))
            {

                del = s;
                break;
            }
        }

        if(del != null)
        {
            audioSessions.remove(del);
            listener.onAudioSessionRemoved(del);
        }
    }

    public void clearAudioSessions()
    {
        for(AudioSession s : audioSessions)
        {
            audioSessions.remove(s);
            listener.onAudioSessionRemoved(s);
        }
    }

    private AudioSession getAudioSession(String sessionId){
        for(AudioSession s : audioSessions)
        {
            if(sessionId.equals(s.id))
                return s;
        }
        return null;
    }

    public List<AudioSession> getAudioSessionListCopy()
    {
        List<AudioSession> ret = new ArrayList<>();
        for(AudioSession s : audioSessions)
        {
            ret.add(s.copy());
        }
        return ret;
    }
}

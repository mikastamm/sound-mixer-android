package mikastamm.com.soundmixer;

import java.util.ArrayList;
import java.util.List;

import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Datamodel.AudioSessionIcon;
import mikastamm.com.soundmixer.Datamodel.DecodedAudioSessionIcon;
import mikastamm.com.soundmixer.Helpers.ImageEncodingFactory;
import mikastamm.com.soundmixer.UI.AudioSessionViewModel;
import mikastamm.com.soundmixer.UI.VolumeSlidersFragment;

public class ClientAudioSessions {
    public String ownerClientId;
    public AudioSessionDelegate listener;

    private List<AudioSession> audioSessions = new ArrayList<>();

    public ClientAudioSessions(){
        listener = new AudioSessionDelegate();
    }

    public void addAudioSession(AudioSession session)
    {
        audioSessions.add(session);
        listener.audioSessionAdded(session);
    }

    public void addAudioSessions(AudioSession[] sessions)
    {
        for(AudioSession session : sessions) {
            audioSessions.add(session);
            listener.audioSessionAdded(session);
        }
    }

    public AudioSessionViewModel getViewModel(VolumeSlidersFragment fragment){
        return new AudioSessionViewModel(audioSessions, listener, fragment);
    }

    public void updateAudioSession(AudioSession newValues)
    {
        AudioSession oldValues;

        for(AudioSession s : audioSessions)
        {
            if(s.id.equals(newValues.id))
            {
                oldValues = s.copy();
                s.updateValues(newValues);

                listener.audioSessionEdited(oldValues, s);
                break;
            }
        }
    }

    public void setAudioSessionImage(AudioSessionIcon icon)
    {
        AudioSession newS = getAudioSession(icon.id);

        if(newS == null)
            return;

        //AudioSessionIcon hold the String representation of the icon
        //We have to convert it to a Bitmap object first
        DecodedAudioSessionIcon decodedIcon = DecodedAudioSessionIcon.fromAudioSessionIcon(icon);
        AudioSessionIconList.getInstance().add(decodedIcon);

        AudioSession old = newS.copy();
        newS.icon = decodedIcon.icon;
        listener.audioSessionEdited(old, newS);
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
            AudioSessionIconList.getInstance().remove(id);
            listener.audioSessionRemoved(del);
        }
    }

    public void clearAudioSessions()
    {
        List<AudioSession> clearedSessions = new ArrayList<AudioSession>(audioSessions);
        audioSessions.clear();
        AudioSessionIconList.getInstance().clear();

        for(AudioSession s : clearedSessions)
        {
            listener.audioSessionRemoved(s);
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

package mikastamm.com.soundmixer;

import java.util.ArrayList;
import java.util.List;

import mikastamm.com.soundmixer.Datamodel.AudioSession;

public class AudioSessionListener {
    public interface AudioSessionChangeListener{
        void onAudioSessionAdded(AudioSession newSession);
        void onAudioSessionRemoved(AudioSession removedSession);
        void onAudioSessionEdited(AudioSession oldSession, AudioSession newSession);
    }
    List<AudioSessionChangeListener> audioSessionChangeListeners = new ArrayList<>();
    public void addAudioSessionChangeListener(AudioSessionChangeListener listener){
        audioSessionChangeListeners.add(listener);
    }

    public void removeAudioSessionChangeListener(AudioSessionChangeListener listener)
    {
        audioSessionChangeListeners.remove(listener);
    }

    public void onAudioSessionAdded(AudioSession newSession)
    {
        for (AudioSessionChangeListener listener : audioSessionChangeListeners) {
            listener.onAudioSessionAdded(newSession);
        }
    }

    void onAudioSessionRemoved(AudioSession removedSession){
        for (AudioSessionChangeListener listener : audioSessionChangeListeners) {
            listener.onAudioSessionRemoved(removedSession);
        }
    }

    void onAudioSessionEdited(AudioSession oldSession, AudioSession newSession){
        for (AudioSessionChangeListener listener : audioSessionChangeListeners) {
            listener.onAudioSessionEdited(oldSession, newSession);
        }
    }
}

package mikastamm.com.soundmixer.UI;

import java.util.ArrayList;
import java.util.List;

import mikastamm.com.soundmixer.AudioSessionDelegate;
import mikastamm.com.soundmixer.Datamodel.AudioSession;

/**
 * Created by Mika on 19.04.2018.
 */

public class AudioSessionViewModel {
    private List<AudioSession> model = new ArrayList<>();
    private AudioSessionDelegate modelDelegate;
    private AudioSessionDelegate.AudioSessionChangeListener modelListener;

    public List<AudioSession> viewModel = new ArrayList<>();
    public AudioSessionDelegate viewModelDelegate = new AudioSessionDelegate();
    private AudioSessionDelegate.AudioSessionChangeListener viewModelListener;

    private VolumeSlidersFragment fragment;

    public AudioSessionViewModel(List<AudioSession> model, AudioSessionDelegate modelDelegate, VolumeSlidersFragment fragment)
    {
        this.model = model;
        this.modelDelegate = modelDelegate;
        this.fragment = fragment;
        makeViewModel();
        setupModelListener();
        setupViewListener();
    }

    public void dispose(){
        modelDelegate.removeAudioSessionChangeListener(modelListener);
        viewModelDelegate.removeAudioSessionChangeListener(viewModelListener);
    }

    private void makeViewModel(){
        for(AudioSession s : model)
        {
            viewModel.add(s.copy());
        }
    }

    private void setupModelListener()
    {
        //Changes sent from the Server to the Client

        modelListener = new AudioSessionDelegate.AudioSessionChangeListener() {
            @Override
            public void onAudioSessionAdded(AudioSession newSession) {
                final AudioSession finalSession = newSession;
                fragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewModel.add(finalSession);
                        fragment.notifyDatasetChanged();
                    }
                });
            }

            @Override
            public void onAudioSessionRemoved(AudioSession removedSession) {
                final AudioSession finalSession = removedSession;
                fragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewModel.remove(finalSession);
                        fragment.notifyDatasetChanged();
                    }
                });
            }

            @Override
            public void onAudioSessionEdited(AudioSession oldSession, AudioSession newSession) {
                final AudioSession finalOldSession = oldSession;
                final AudioSession finalNewSession = newSession;
                fragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(AudioSession s : viewModel)
                        {
                            if(s.id.equals(finalOldSession.id))
                            {
                                s.updateValues(finalNewSession);
                                break;
                            }
                        }
                        fragment.notifyDatasetChanged();
                    }
                });
            }
        };
        modelDelegate.addAudioSessionChangeListener(modelListener);
    }

    public void setTracking(String sessionId, boolean tracking)
    {
        if(tracking)
            fragment.activeServerLogic.sendTrackStart(sessionId);
        else
            fragment.activeServerLogic.sendTrackEnd(sessionId);
    }

    private void setupViewListener(){
        //Changes made by user
        viewModelListener = new AudioSessionDelegate.AudioSessionChangeListener() {
            @Override
            public void onAudioSessionEdited(AudioSession oldSession, AudioSession newSession) {
                if(oldSession.isTracking != newSession.isTracking)
                    setTracking(newSession.id, newSession.isTracking);

                fragment.activeServerLogic.sendAudioSessionChanged(newSession);
            }

            @Override public void onAudioSessionAdded(AudioSession newSession) {}
            @Override public void onAudioSessionRemoved(AudioSession removedSession) {}

        };
        viewModelDelegate.addAudioSessionChangeListener(viewModelListener);
    }
}

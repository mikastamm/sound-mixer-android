package mikastamm.com.soundmixer.UI;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.lucasr.twowayview.TwoWayView;

import mikastamm.com.soundmixer.AudioSessionDelegate;
import mikastamm.com.soundmixer.ClientAudioSessions;
import mikastamm.com.soundmixer.ClientAudioSessionsManager;
import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.ServerLogic;
import mikastamm.com.soundmixer.R;

/**
 * Created by Mika on 03.04.2018.
 */

public class VolumeSlidersFragment extends Fragment {
    //Provides a handle for the active server to send, receive and handle data
    //the active server is the one that's currently displayed one the main screen (VolumeSlidersFragment)
    public ServerLogic activeServerLogic;
    public Server server;

    private TwoWayView listView;
    private VolumeSeekbarsListViewAdapter adapter;

    private ClientAudioSessions audioSessions;
    private AudioSessionViewModel audioSessionViewModel;

    public static VolumeSlidersFragment newInstanceAndConnect(Server server){
        VolumeSlidersFragment fragment = new VolumeSlidersFragment();
        fragment.server = server;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activeServerLogic = new ServerLogic(server, getActivity());
        activeServerLogic.connectAndStartCommunicating();

        audioSessions = ClientAudioSessionsManager.getClientAudioSessions(server.id);
        audioSessionViewModel = audioSessions.getViewModel(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.volume_slider_fragment, container, false);
        listView = layout.findViewById(R.id.lvVolumeSliders);
        adapter = new VolumeSeekbarsListViewAdapter(getActivity(), audioSessionViewModel.viewModel, audioSessionViewModel.viewModelDelegate);
        listView.setOrientation(TwoWayView.Orientation.VERTICAL);
        listView.setAdapter(adapter);

        return layout;

    }

    @Override
    public void onStop(){
        super.onStop();

    }

    public void notifyDatasetChanged(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void runOnUiThread(Runnable r)
    {
        new Handler(Looper.getMainLooper()).post(r);
    }
}

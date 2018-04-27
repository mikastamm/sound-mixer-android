package mikastamm.com.soundmixer.UI;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.lucasr.twowayview.TwoWayView;

import java.io.Serializable;

import mikastamm.com.soundmixer.ClientAudioSessions;
import mikastamm.com.soundmixer.ClientAudioSessionsManager;
import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.Exceptions.ServerNotFoundException;
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


    public static VolumeSlidersFragment newInstanceAndConnect(Server server) {
        VolumeSlidersFragment fragment = new VolumeSlidersFragment();
        fragment.server = server;
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("server", server);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Don't recreate this fragment on orientation change, keep the existing instance
        setRetainInstance(true);

        recoverServer(savedInstanceState);
        activeServerLogic = new ServerLogic(server, getActivity());
        activeServerLogic.connectAndStartCommunicating();
        audioSessions = ClientAudioSessionsManager.getClientAudioSessions(server.id);
        audioSessionViewModel = audioSessions.getViewModel(this);
    }



    private void recoverServer(Bundle savedInstanceState) {
        if (server == null) {
            Serializable retrieved = savedInstanceState.getSerializable("server");
            if (retrieved == null)
                throw new ServerNotFoundException("Server was null and no Server was found in savedInstanceState");

            server = (Server)retrieved;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.volume_slider_fragment, container, false);
        listView = layout.findViewById(R.id.lvVolumeSliders);
        adapter = new VolumeSeekbarsListViewAdapter(getActivity(), audioSessionViewModel.viewModel, audioSessionViewModel.viewModelDelegate);

        // if(orientation == Configuration.ORIENTATION_PORTRAIT)
        listView.setOrientation(TwoWayView.Orientation.VERTICAL);
        // else if(orientation == Configuration.ORIENTATION_LANDSCAPE)
        //    listView.setOrientation(TwoWayView.Orientation.HORIZONTAL);

        listView.setAdapter(adapter);

        return layout;

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        audioSessionViewModel.dispose();
    }

    public void notifyDatasetChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void runOnUiThread(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }
}

package com.nulldozer.volumecontrol;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by Mika on 26.09.2017.
 */
public class ClientFragment extends Fragment {

    public ClientThread clientThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }
}

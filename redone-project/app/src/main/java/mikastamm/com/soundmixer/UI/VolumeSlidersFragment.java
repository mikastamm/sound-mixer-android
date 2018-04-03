package mikastamm.com.soundmixer.UI;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mikastamm.com.soundmixer.R;

/**
 * Created by Mika on 03.04.2018.
 */

public class VolumeSlidersFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.volume_slider_fragment, container, false);
    }

}

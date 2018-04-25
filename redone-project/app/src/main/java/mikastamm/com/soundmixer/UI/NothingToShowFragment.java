package mikastamm.com.soundmixer.UI;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import mikastamm.com.soundmixer.R;

/**
 * Created by Mika on 25.04.2018.
 */

public class NothingToShowFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.no_volume_sliders_fragment, container, false);
        layout.findViewById(R.id.scrollViewNothingToShow).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        return layout;
    }
}

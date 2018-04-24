package mikastamm.com.soundmixer.UI;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import mikastamm.com.soundmixer.R;

/**
 * Created by Mika on 23.04.2018.
 */

public class GettingStartedFragment extends Fragment {
    private boolean visible = false;
    private LinearLayout contentLayout;
    private FrameLayout background;
    private View baseLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.getting_started_overlay_fragment, container, false);

        if(!visible)
            layout.setVisibility(View.GONE);

        baseLayout = layout;

        layout.findViewById(R.id.buttonHideGettingStarted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });

        contentLayout = layout.findViewById(R.id.llGettingStartedContent);
        background = layout.findViewById(R.id.flGettingStartedOverlay);

        return layout;
    }

    public void toggle(){
        if(visible)
            hide();
        else
            show();
    }

    public void show(){
        baseLayout.setVisibility(View.VISIBLE);
        visible = true;
        showContent();
        fadeInBackground();
    }

    private void showContent(){
        Animation slide_up = AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_up);
        slide_up.setInterpolator(new AccelerateDecelerateInterpolator());
        contentLayout.startAnimation(slide_up);
    }

    private void fadeInBackground(){
        Animation fade_in = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        fade_in.setInterpolator(new AccelerateDecelerateInterpolator());
        background.startAnimation(fade_in);
        background.setAlpha(.5f);
    }

    public void hide(){
        hideContent();
        fadeOutBackground();
        visible = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(getResources().getInteger(R.integer.gettingStartedAnimDuration));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       baseLayout.setVisibility(View.GONE);

                   }
               });
            }
        }).start();

    }

    private void hideContent(){
        Animation slide_down = AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_down);
        slide_down.setInterpolator(new AccelerateDecelerateInterpolator());
        contentLayout.startAnimation(slide_down);

    }

    private void fadeOutBackground(){
        Animation fade_out = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        fade_out.setInterpolator(new AccelerateDecelerateInterpolator());
        background.startAnimation(fade_out);
    }
}

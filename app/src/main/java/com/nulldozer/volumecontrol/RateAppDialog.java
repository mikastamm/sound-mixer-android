package com.nulldozer.volumecontrol;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

/**
 * Created by Mika on 31.10.2017.
 */

public class RateAppDialog extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.rate_app_prompt, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnSubmit = (Button)view.findViewById(R.id.btnSubmitRating);
        final RatingBar ratingBar = (RatingBar)view.findViewById(R.id.ratingBar);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = ratingBar.getRating();

                if(rating < 4)
                {
                    FeedbackDialog feedbackDialog = new FeedbackDialog();
                    feedbackDialog.setFeedbackType(FeedbackDialog.FeedbackType.BAD_FEEDBACK);
                    feedbackDialog.show(MainActivity.Instance.getSupportFragmentManager(), "feedback-dialog");
                    dismiss();
                }
                else{
                    RateOnPlayDialog rateDialog = new RateOnPlayDialog();
                    rateDialog.show(MainActivity.Instance.getSupportFragmentManager(), "rate-on-play-dialog");
                    dismiss();
                }
            }
        });
    }
}

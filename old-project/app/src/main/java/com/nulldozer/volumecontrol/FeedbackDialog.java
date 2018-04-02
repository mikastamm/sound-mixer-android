package com.nulldozer.volumecontrol;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class FeedbackDialog extends DialogFragment{

    private FeedbackType feedbackType = FeedbackType.NEUTRAL_FEEDBACK;
    private MainActivity mainActivity;

    public enum FeedbackType{
        BAD_FEEDBACK,
        NEUTRAL_FEEDBACK
    }

    public void setMainActivity(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
    }
    public void setFeedbackType(FeedbackType type){
        feedbackType = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.send_feedback_dialog, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button btnSend = (Button)view.findViewById(R.id.btnSendFeedback);
        final EditText etMessage = (EditText)view.findViewById(R.id.editTextFeedbackText);
        final EditText etEmail = (EditText)view.findViewById(R.id.editTextFeedbackEmail);
        final TextView tvTitle = (TextView)view.findViewById(R.id.tvFeedbackDialogTitle);

        if(feedbackType == FeedbackType.NEUTRAL_FEEDBACK)
        {
            etMessage.setHint(R.string.feedback_hint_neutral_feedback);
            tvTitle.setText(R.string.feedback_title_neutral_feedback);
        }
        else if(feedbackType == FeedbackType.BAD_FEEDBACK)
        {
            etMessage.setHint(R.string.feedback_hint_bad_feedback);
            tvTitle.setText(R.string.feedback_title_bad_feedback);
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg, email, toSend;
                msg = etMessage.getText().toString();
                email = etEmail.getText().toString();

                if(msg.isEmpty())
                {
                    dismiss();
                }
                else{
                    toSend = msg + " ;User Email:" + email;
                    new SendMailTask(mainActivity).execute("SoundMixerInAppFeedback@gmail.com", "SoundMixerApp", "feedback@nulldozer.me", "User Feedback", toSend);
                    Toast.makeText(mainActivity, getString(R.string.thanks_for_feedback), Toast.LENGTH_LONG).show();
                    dismiss();
                }

            }
        });
    }
}

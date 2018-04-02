package com.nulldozer.volumecontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class feedback extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

    }
}

package com.nulldozer.volumecontrol;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Mika on 02.11.2017.
 */

public class Fullscreen {

    AppCompatActivity activity;
    Window window;

    public Fullscreen(AppCompatActivity activity)
    {
        this.activity = activity;
        this.window = activity.getWindow();
    }

    public Fullscreen(Window window)
    {
        this.window = window;
    }

    public void enable(){
        if(Build.VERSION.SDK_INT < 19) {
            if(activity != null)
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE);

            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else{
            hideSystemUI(window.getDecorView());
        }
    }

    public void disable()
    {
        if(Build.VERSION.SDK_INT > 19)
        hideSystemUI(window.getDecorView());
    }

    // This snippet hides the system bars.
    @TargetApi(19)
    private static void hideSystemUI(View decorView) {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        decorView.setSystemUiVisibility(

                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    @TargetApi(19)
    private static void showSystemUI(View decorView) {
        decorView.setSystemUiVisibility(View.VISIBLE);
    }

}

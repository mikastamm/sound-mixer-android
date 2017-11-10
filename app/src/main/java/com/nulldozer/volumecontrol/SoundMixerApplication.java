package com.nulldozer.volumecontrol;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by Mika on 10.11.2017.
 */

public class SoundMixerApplication extends Application {

    private RefWatcher refWatcher;

    @Override public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            Log.e("Application Class", "####\n#########\n###########\n##\n###############\n##############Analyzer Process started leak canary");
            return;
        }
        Log.e("Application Class", "####\n#########\n###########\n##\n###############\n##############Application class called");
        refWatcher = LeakCanary.install(this);
        // Normal app init code...
    }


    public static RefWatcher getRefWatcher(Context context) {
        SoundMixerApplication application = (SoundMixerApplication) context.getApplicationContext();
        return application.refWatcher;
    }
}

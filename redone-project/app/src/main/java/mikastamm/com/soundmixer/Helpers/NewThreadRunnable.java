package mikastamm.com.soundmixer.Helpers;

import android.icu.text.RelativeDateTimeFormatter;

/**
 * Created by Mika on 24.04.2018.
 */

public class NewThreadRunnable implements Runnable {
    private Runnable innerRunnable;

    public NewThreadRunnable(Runnable runnable)
    {
        this.innerRunnable = runnable;
    }

    @Override
    public void run() {
        new Thread(innerRunnable).start();
    }
}

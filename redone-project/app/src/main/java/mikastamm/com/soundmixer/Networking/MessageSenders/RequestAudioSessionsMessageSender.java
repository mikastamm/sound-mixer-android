package mikastamm.com.soundmixer.Networking.MessageSenders;

import android.util.Log;

import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.Connection;

/**
 * Created by Mika on 20.04.2018.
 */

public class RequestAudioSessionsMessageSender implements Runnable {
    private Connection connection;
    public final String messageTag = "GETAUDIOSESSIONS";

    public RequestAudioSessionsMessageSender(Connection connection)
    {
        this.connection = connection;
    }

    @Override public void run() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connection.writeLine(messageTag);
    }
}

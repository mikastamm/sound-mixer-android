package mikastamm.com.soundmixer.Networking.MessageSenders;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.Connection;

/**
 * Created by Mika on 20.04.2018.
 */

public class RequestAudioSessionsMessageSender extends MessageSender {
    Connection connection;

    public RequestAudioSessionsMessageSender(Connection connection)
    {
        this.connection = connection;
    }

    @Override
    public void send() {
        final String msg = "GETAUDIOSESSIONS";

        Runnable request = new Runnable() {@Override public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connection.writeLine(msg);
            startNextMessageSender();
        }};
        new Thread(request).start();

        Log.i(MainActivity.TAG, "Requested Audio Sessions");

    }

}

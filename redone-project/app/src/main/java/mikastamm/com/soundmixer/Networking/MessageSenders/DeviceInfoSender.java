package mikastamm.com.soundmixer.Networking.MessageSenders;

import android.os.Build;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import mikastamm.com.soundmixer.BuildConfig;
import mikastamm.com.soundmixer.Datamodel.Device;
import mikastamm.com.soundmixer.Helpers.Json;
import mikastamm.com.soundmixer.MainActivity;
import mikastamm.com.soundmixer.Networking.Connection;
import mikastamm.com.soundmixer.Networking.ServerConnection;

/**
 * Created by Mika on 18.04.2018.
 */

public class DeviceInfoSender extends MessageSender {
    public static String messageTag = "DEVINFO";

    private Connection connection;

    public DeviceInfoSender(Connection connection)
    {
        this.connection = connection;
    }

    private Runnable sendInfoRunnable = new Runnable() {
        @Override
        public void run() {
            if(connection != null)
            {
                Device device = new Device();
                device.Name = Build.MODEL;
                device.Version = BuildConfig.VERSION_CODE;
                device.ID = getDeviceId();

                connection.writeLine(messageTag + Json.serialize(device));
                Log.i(MainActivity.TAG, "Sent Device info");
            }
            else
                Log.i(MainActivity.TAG, "Server Connection Object is null or not connected");

            startNextMessageSender();
        }
    };

    private String getDeviceId()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(android.os.Build.MANUFACTURER);
        sb.append(".");
        sb.append(android.os.Build.MODEL);
        sb.append(".");
        sb.append(android.os.Build.SERIAL);

        return sb.toString();
    }

    @Override
    public void send(){
        new Thread(sendInfoRunnable).start();
    }
}

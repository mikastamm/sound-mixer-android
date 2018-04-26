package mikastamm.com.soundmixer.Networking;

import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.Helpers.Json;
import mikastamm.com.soundmixer.ServerList;

/**
 * Created by Mika on 04.04.2018.
 */

public class GetInfoFromServerRunnable implements Runnable {
    private Socket socket;
    private Connection connection;

    public GetInfoFromServerRunnable(Socket socket){
        this.socket = socket;
        connection = new ConnectionFactory().makeConnection(socket);
    }

    @Override
    public void run() {
        connection.connect();
        String serverData = null;

        try{
            serverData = connection.readLine();
            Log.i("sound-mixer-log", serverData);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if(serverData != null)
        {
            Server server = Json.deserialize(serverData, Server.class);

            if(server != null)
            {
                server.ipAddress = socket.getInetAddress().getHostAddress();
                ServerList.getInstance().addServer(server);
                Log.i("sound-mixer-log", "Discovered " + server.name);
            }
        }

        connection.dispose();
    }
}

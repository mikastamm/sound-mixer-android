package mikastamm.com.soundmixer.Networking;

import java.io.IOException;

import mikastamm.com.soundmixer.Datamodel.Server;

/**
 * Created by Mika on 28.03.2018.
 */

public interface ServerConnection {
    Server getServer();
    void writeLine(String line);
    String readLine() throws IOException;
    void connect();
    void dispose();
    boolean isConnected();
}

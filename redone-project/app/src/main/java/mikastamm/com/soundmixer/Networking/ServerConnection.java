package mikastamm.com.soundmixer.Networking;

import java.io.IOException;

/**
 * Created by Mika on 28.03.2018.
 */

public interface ServerConnection {
    void writeLine(String line);
    String readLine() throws IOException;
    void connect();
    void dispose();
    boolean isConnected();
}

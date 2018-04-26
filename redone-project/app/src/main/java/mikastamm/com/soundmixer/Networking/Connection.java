package mikastamm.com.soundmixer.Networking;

import java.io.IOException;

/**
 * Created by Mika on 17.04.2018.
 */

public interface Connection {
    void writeLine(String line);
    String readLine() throws IOException;
    void connect();
    void dispose();

    boolean isConnected();
}

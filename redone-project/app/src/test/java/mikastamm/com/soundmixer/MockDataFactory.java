package mikastamm.com.soundmixer;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Datamodel.Server;

/**
 * Created by Mika on 27.03.2018.
 */

public class MockDataFactory {
    public static String[] getRSAKeys(int amount){
        String[] res = new String[amount];

        for(int i = 0; i < amount; i++)
        {
            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(1024);
                byte[] publicKey = keyGen.genKeyPair().getPublic().getEncoded();
                StringBuffer retString = new StringBuffer();
                for (int j = 0; j < publicKey.length; ++j) {
                    retString.append(Integer.toHexString(0x0100 + (publicKey[j] & 0x00FF)).substring(1));
                }
                res[i] = retString.toString();
            }
            catch(NoSuchAlgorithmException e){}
        }

        return res;
    }


    private static int currentAudioSessionId = 0;
    public static List<AudioSession> getAudioSessions(int count)
    {
        Random r = new Random();
        List<AudioSession> sessions = new ArrayList<>();
        for(int i = 0; i< count; i++)
        {
            AudioSession s = new AudioSession();
            s.mute = r.nextBoolean() | r.nextBoolean();
            s.volume = r.nextFloat() * 100;

            int nameLen = r.nextInt() % 7 + 1;
            s.title = getRandomString(nameLen);
            s.id = Integer.toString(currentAudioSessionId);
            currentAudioSessionId++;

            sessions.add(s);
        }
        return sessions;
    }

    public static List<Server> getServer(int count)
    {
        Random r = new Random();
        List<Server> servers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Server s = new Server();
            s.name = getRandomString(6);
            s.hasPassword = false;
            s.id = getRSAKeys(1)[0];
            s.ipAddress = "192.168.0."+Integer.toString(i);
            servers.add(s);
        }
        return servers;
    }

    private static char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    public static String getRandomString(int len)
    {
        Random r = new Random();
        StringBuilder s = new StringBuilder();
        for (int j = 0; j < len; j++)
        {
            int index = Math.abs(r.nextInt() % chars.length);
            s.append(chars[index]);
        }
        return s.toString();
    }
}

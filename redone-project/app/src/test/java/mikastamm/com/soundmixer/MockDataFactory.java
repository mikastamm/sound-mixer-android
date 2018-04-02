package mikastamm.com.soundmixer;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mikastamm.com.soundmixer.Datamodel.AudioSession;

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

    private static char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
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
            for (int j = 0; j < nameLen; j++)
            {
                s.title += chars[r.nextInt() % chars.length];
            }
            s.id = Integer.toString(currentAudioSessionId);
            currentAudioSessionId++;

            sessions.add(s);
        }
        return sessions;
    }
}

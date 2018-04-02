package mikastamm.com.soundmixer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Datamodel.Server;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

/**
 * Created by Mika on 02.04.2018.
 */

public class AndroidMockDataFactory {
    public static List<String> getEncodedSessionIcons(int count)
    {
        List<String> ret = new ArrayList<>();
        List<Bitmap> bitmaps = getSessionIcons(count);
        for (int i = 0; i<count; i++)
        {
            Bitmap bmp = bitmaps.get(i);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            ret.add(Base64.encodeToString(byteArray, Base64.NO_WRAP));
        }
        return ret;
    }

    public static List<Bitmap> getSessionIcons(int count)
    {
        List<Bitmap> ret = new ArrayList<>();
        for (int i = 0; i<count; i++)
        {
            ret.add(textAsBitmap(Integer.toString(i), 16, Color.RED));
        }
        return ret;
    }

    private static Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

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

    private static char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static String getRandomString(int len)
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

    public static List<Server> getServer(int count)
    {
        Random r = new Random();
        List<Server> servers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Server s = new Server();
            s.name = getRandomString(6);
            s.hasPassword = false;
            s.id = getRSAKeys(1)[0];
            s.IPAddress = "192.168.0."+Integer.toString(i);
            servers.add(s);
        }
        return servers;
    }
}

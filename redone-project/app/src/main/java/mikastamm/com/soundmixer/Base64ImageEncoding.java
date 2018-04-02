package mikastamm.com.soundmixer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mika on 02.04.2018.
 */

public class Base64ImageEncoding implements ImageEncoding {
    @Override
    public Bitmap decode(String encoded) {
        byte[] imgBytes = Base64.decode(encoded, Base64.NO_WRAP);
        return BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
    }

    @Override
    public List<Bitmap> decode(List<String> encoded) {
        List<Bitmap> ret = new ArrayList<>();
        for(int i = 0; i < encoded.size(); i++)
        {
            ret.add(decode(encoded.get(i)));
        }
        return ret;
    }

    @Override
    public String encode(Bitmap decoded) {
        if(decoded == null)
            return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        decoded.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    @Override
    public List<String> encode(List<Bitmap> decoded) {
        List<String> ret = new ArrayList<>();
        for (int i = 0; i<decoded.size(); i++)
        {
            ret.add(encode(decoded.get(i)));
        }
        return ret;
    }
}

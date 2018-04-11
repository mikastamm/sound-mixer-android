package mikastamm.com.soundmixer.Helpers;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by Mika on 02.04.2018.
 */

public interface ImageEncoding {
    Bitmap decode(String encoded);
    List<Bitmap> decode(List<String> encoded);

    String encode(Bitmap decoded);
    List<String> encode(List<Bitmap> decoded);
}

package mikastamm.com.soundmixer.Helpers;

import mikastamm.com.soundmixer.Helpers.Base64ImageEncoding;
import mikastamm.com.soundmixer.Helpers.ImageEncoding;

/**
 * Created by Mika on 02.04.2018.
 */

public class ImageEncodingFactory {
    public static ImageEncoding getStandardEncoding(){
        return new Base64ImageEncoding();
    }
}

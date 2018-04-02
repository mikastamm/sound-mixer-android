package mikastamm.com.soundmixer;

/**
 * Created by Mika on 02.04.2018.
 */

public class ImageEncodingFactory {
    public static ImageEncoding getStandardEncoding(){
        return new Base64ImageEncoding();
    }
}

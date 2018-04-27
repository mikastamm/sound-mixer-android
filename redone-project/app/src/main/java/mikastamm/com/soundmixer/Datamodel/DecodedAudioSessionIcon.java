package mikastamm.com.soundmixer.Datamodel;

import android.graphics.Bitmap;

import mikastamm.com.soundmixer.Exceptions.InvalidAudioSessionIconException;
import mikastamm.com.soundmixer.Helpers.ImageEncodingFactory;

/**
 * Created by Mika on 27.04.2018.
 */

public class DecodedAudioSessionIcon {
    public String id;
    public Bitmap icon;

    public static DecodedAudioSessionIcon fromAudioSessionIcon(AudioSessionIcon icon){
        if(icon.icon == null)
            throw new InvalidAudioSessionIconException("Application Id: " + icon.id + " icon was null");

        DecodedAudioSessionIcon decodedIcon = new DecodedAudioSessionIcon();
        decodedIcon.id = icon.id;
        decodedIcon.icon = ImageEncodingFactory.getStandardEncoding().decode(icon.icon);

        if(decodedIcon.icon == null)
            throw new InvalidAudioSessionIconException("Couldn't decode AudioSessionIcon's icon (id=" + icon.id+")");

        return decodedIcon;
    }
}

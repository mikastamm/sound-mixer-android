package mikastamm.com.soundmixer.Exceptions;

/**
 * Created by Mika on 09.04.2018.
 */

public class InvalidServerException extends Exception {
    public InvalidServerException(){}
    public InvalidServerException(String message)
    {
        super(message);
    }
}

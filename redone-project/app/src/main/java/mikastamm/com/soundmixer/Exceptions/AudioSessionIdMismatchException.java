package mikastamm.com.soundmixer.Exceptions;

public class AudioSessionIdMismatchException extends RuntimeException {
    public AudioSessionIdMismatchException() {
    }

    public AudioSessionIdMismatchException(String message) {
        super(message);
    }
}

package mikastamm.com.soundmixer.Exceptions;

public class ServerNotFoundException extends RuntimeException {
    public ServerNotFoundException() {
    }

    public ServerNotFoundException(String message) {
        super(message);
    }
}

package mikastamm.com.soundmixer.Exceptions;

public class InvalidConfigurationException extends RuntimeException {
    public InvalidConfigurationException() {
    }

    public InvalidConfigurationException(String message) {
        super(message);
    }
}

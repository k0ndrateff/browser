package error;

public class NotImplementedException extends RuntimeException {
    public NotImplementedException() {
        super("This feature is not implemented yet.");
    }

    public NotImplementedException(String feature) {
        super(feature + " is not implemented yet.");
    }
}

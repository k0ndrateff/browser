package error;

public class Logger {
    public static void error(Exception e) {
        System.err.println("An unexpected error has occurred: " + e.getMessage());
    }
}

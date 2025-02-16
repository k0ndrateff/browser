package error;

import java.time.Instant;

public class Logger {
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";

    public static void verbose(String message) {
        System.out.println(ANSI_PURPLE + "<" + Instant.now() + "> " + message + ANSI_RESET);
    }

    public static void performance(String message) {
        System.out.println(ANSI_GREEN + "<" + Instant.now() + "> " + message + ANSI_RESET);
    }

    public static void error(Exception e) {
        System.err.println("<" + Instant.now() + "> " + "An unexpected error has occurred: " + e.getMessage());
    }
}

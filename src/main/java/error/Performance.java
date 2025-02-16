package error;

import java.util.HashMap;
import java.util.Map;

public class Performance {
    private static final Map<String, Long> times = new HashMap<>();

    public static void start(String name) {
        times.put(name, System.nanoTime());
    }

    public static void measure(String name) {
        long time = System.nanoTime() - times.get(name);
        long ms = time / 1000000;
        long mcs = time / 1000;

        Logger.performance(name + " took " + mcs + "µs (" + ms + "ms)");
        times.remove(name);
    }
}

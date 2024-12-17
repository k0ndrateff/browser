package core;

import core.request.URL;

public class Browser {
    private static final String DEFAULT_URL = "file://test.html";
    private static final CachingController cachingController = new CachingController("./cache");

    public static CachingController getCachingController() {
        return cachingController;
    }

    public static void main(String[] args) {
        cachingController.init();

        URL url = new URL(Browser.DEFAULT_URL);

        if (args.length > 0) {
            url = new URL(args[0]);
        }

        PageController.load(url);

        PageController.shutdown();
        cachingController.shutdown();
    }
}

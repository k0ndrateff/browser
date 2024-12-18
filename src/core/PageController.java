package core;

import core.request.HTTPRequest;
import core.request.URL;

import java.io.IOException;

public class PageController {
    public static String load(URL url) {
        try {
            String body = url.request();

            if (url.isViewSource()) {
                return HTML.source(body);
            }
            else {
                return HTML.onlyText(body);
            }
        }
        catch (IOException e) {
            System.err.println("Unable to load page: " + e.getMessage());
        }

        return null;
    }

    public static void shutdown() {
        HTTPRequest.closeAllSockets();
    }
}

package core;

import java.io.IOException;

public class PageController {
    public static void load(URL url) {
        try {
            String body = url.request();

            if (url.isViewSource()) {
                HTML.printSource(body);
            }
            else {
                HTML.printOnlyText(body);
            }
        }
        catch (IOException e) {
            System.err.println("Unable to load page: " + e.getMessage());
        }
    }
}

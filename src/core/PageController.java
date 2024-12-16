package core;

import java.io.IOException;

public class PageController {
    public static void load(URL url) {
        try {
            String body = url.request();

            HTML.printOnlyText(body);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

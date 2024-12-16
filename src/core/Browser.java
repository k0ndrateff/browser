package core;

public class Browser {
    private static final String DEFAULT_URL = "file://test.html";

    public static void main(String[] args) {
        URL url = new URL(Browser.DEFAULT_URL);

        if (!args[0].isEmpty()) {
            url = new URL(args[0]);
        }

        PageController.load(url);
    }
}

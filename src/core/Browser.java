package core;

public class Browser {
    public static void main(String[] args) {
        URL url = new URL(args[0]);

        PageController.load(url);
    }
}

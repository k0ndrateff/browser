package core;

public class HTML {
    public static void printOnlyText(String html) {
        boolean inTag = false;

        for (char c : html.toCharArray()) {
            if (c == '<') {
                inTag = true;
            }
            else if (c == '>') {
                inTag = false;
            }
            else if (!inTag) {
                System.out.print(c);
            }
        }
    }
}

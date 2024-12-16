package core;

public class HTML {
    public static void printSource(String html) {
        System.out.println(html);
    }

    private static char resolveEntity(String entity) {
        return switch (entity) {
            case "&lt" -> '<';
            case "&gt" -> '>';
            default -> entity.charAt(0);
        };
    }

    public static void printOnlyText(String html) {
        boolean inTag = false;
        StringBuilder entity = new StringBuilder();

        for (char c : html.toCharArray()) {
            if (c == '<') {
                inTag = true;
            }
            else if (c == '>') {
                inTag = false;
            }
            else if (!inTag) {
                if (c == '&') {
                    entity.append("&");
                }
                else if (c == ';' && !entity.isEmpty()) {
                    System.out.println(HTML.resolveEntity(entity.toString()));
                    entity.setLength(0);
                }
                else if (!entity.isEmpty()) {
                    entity.append(c);
                }
                else {
                    System.out.print(c);
                }
            }
        }
    }
}

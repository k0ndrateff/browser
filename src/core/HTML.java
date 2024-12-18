package core;

public class HTML {
    public static String source(String html) {
        return html;
    }

    private static char resolveEntity(String entity) {
        return switch (entity) {
            case "&lt" -> '<';
            case "&gt" -> '>';
            default -> entity.charAt(0);
        };
    }

    public static String onlyText(String html) {
        StringBuilder result = new StringBuilder();

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
                    result.append(HTML.resolveEntity(entity.toString()));
                    entity.setLength(0);
                }
                else if (!entity.isEmpty()) {
                    entity.append(c);
                }
                else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }
}

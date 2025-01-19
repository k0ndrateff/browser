package document;

import error.Logger;

public class HtmlDocument {
    private final String content;

    public HtmlDocument(String content) {
        this.content = content;
    }

    public String getHtml() {
        return content;
    }

    public String getContent() {
        boolean inTag = false;
        boolean inEntity = false;

        StringBuilder contentBuilder = new StringBuilder();
        StringBuilder entityBuilder = new StringBuilder();

        for (char c : content.toCharArray()) {
            if (c == '<') {
                inTag = true;
            } else if (c == '>') {
                inTag = false;
            } else if (!inTag) {
                if (c == '&') {
                    inEntity = true;
                }
                else if (c == ';' && inEntity) {
                    inEntity = false;
                    contentBuilder.append(resolveEntity(entityBuilder.toString()));
                    entityBuilder.setLength(0);
                }
                else if (inEntity) {
                    entityBuilder.append(c);
                }
                else {
                    contentBuilder.append(c);
                }
            }
        }

        return contentBuilder.toString();
    }

    private String resolveEntity(String entity) {
        Logger.verbose("Resolving HTML entity: " + entity);

        return switch (entity) {
            case "lt" -> "<";
            case "gt" -> ">";
            case "amp" -> " &";
            case "quot" -> "\"";
            case "apos" -> "'";
            case "nbsp" -> " ";
            default -> "";
        };
    }
}

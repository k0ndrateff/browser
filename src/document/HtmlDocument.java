package document;

import error.Logger;

import java.util.ArrayList;

public class HtmlDocument {
    private final String content;

    public HtmlDocument(String content) {
        this.content = content;
    }

    public ArrayList<Entity> getHtml() {
        ArrayList<Entity> entities = new ArrayList<>();
        entities.add(new Text(content));

        return entities;
    }

    public String getHtmlString() {
        return content;
    }

    public ArrayList<Entity> getContent() {
        boolean inTag = false;
        boolean inEntity = false;

        ArrayList<Entity> entities = new ArrayList<>();

        StringBuilder buffer = new StringBuilder();
        StringBuilder htmlEntityBuilder = new StringBuilder();

        for (char c : content.toCharArray()) {
            if (c == '<') {
                inTag = true;

                if (!buffer.isEmpty()) {
                    entities.add(new Text(buffer.toString()));
                }

                buffer.setLength(0);
            } else if (c == '>') {
                inTag = false;

                entities.add(new Tag(buffer.toString()));

                buffer.setLength(0);
            } else if (!inTag) {
                if (c == '&') {
                    inEntity = true;
                }
                else if (c == ';' && inEntity) {
                    inEntity = false;
                    buffer.append(resolveEntity(htmlEntityBuilder.toString()));
                    htmlEntityBuilder.setLength(0);
                }
                else if (inEntity) {
                    htmlEntityBuilder.append(c);
                }
                else {
                    buffer.append(c);
                }
            }
            else {
                buffer.append(c);
            }
        }

        if (!buffer.isEmpty() && !inTag) {
            entities.add(new Text(buffer.toString()));
        }

        return entities;
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

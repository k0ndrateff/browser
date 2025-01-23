package document;

import error.Logger;
import rendering.TextRenderer;

import java.util.ArrayList;

public class HtmlDocument {
    private final String content;
    private boolean isRtl;
    private boolean isViewSource;

    public HtmlDocument(String content) {
        this.content = content;
    }

    private ArrayList<HtmlLayoutEntity> parseHtml() {
        ArrayList<HtmlLayoutEntity> entities = new ArrayList<>();
        entities.add(new HtmlText(content));

        return entities;
    }

    public String getHtmlString() {
        return content;
    }

    private ArrayList<HtmlLayoutEntity> parseContent() {
        boolean inTag = false;
        boolean inEntity = false;

        ArrayList<HtmlLayoutEntity> entities = new ArrayList<>();

        StringBuilder buffer = new StringBuilder();
        StringBuilder htmlEntityBuilder = new StringBuilder();

        for (char c : content.toCharArray()) {
            if (c == '<') {
                inTag = true;

                if (!buffer.isEmpty()) {
                    entities.add(new HtmlText(buffer.toString()));
                }

                buffer.setLength(0);
            } else if (c == '>') {
                inTag = false;

                entities.add(new HtmlTag(buffer.toString()));

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
            entities.add(new HtmlText(buffer.toString()));
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
            case "shy" -> TextRenderer.SOFT_HYPHEN_STRING;
            default -> "";
        };
    }

    public ArrayList<HtmlLayoutEntity> getContent() {
        if (isViewSource)
            return parseHtml();
        else
            return parseContent();
    }

    public void setRtl(boolean rtl) {
        this.isRtl = rtl;
    }

    public boolean isRtl() {
        return isRtl;
    }

    public void setViewSource(boolean viewSource) {
        this.isViewSource = viewSource;
    }
}

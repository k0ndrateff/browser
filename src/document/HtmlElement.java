package document;

import java.util.HashMap;

public class HtmlElement extends HtmlNode {
    private final String name;
    private final HashMap<String, String> attributes;

    public HtmlElement(String name, HtmlNode parent, HashMap<String, String> attributes) {
        super(parent);

        this.name = name;
        this.attributes = attributes;
    }

    public static HtmlElement create(String tag, HtmlNode parent) {
        String tagName = "";
        HashMap<String, String> attributes = new HashMap<>();
        StringBuilder token = new StringBuilder();
        String attributeName = "";

        boolean isReadingAttributes = false;
        boolean inQuotedAttribute = false;

        for (char c : tag.toCharArray()) {
            if (isReadingAttributes) {
                if (c == '=') {
                    attributeName = token.toString().toLowerCase();
                    token.setLength(0);
                }
                else if (c == '"') {
                    inQuotedAttribute = !inQuotedAttribute;
                }
                else if (c == ' ' && !inQuotedAttribute) {
                    attributes.put(attributeName, token.toString());
                    attributeName = "";
                    token.setLength(0);
                }
                else {
                    token.append(c);
                }
            }
            else {
                if (c == ' ') {
                    tagName = token.toString().toLowerCase();
                    token.setLength(0);
                    isReadingAttributes = true;
                }
                else {
                    token.append(c);
                }
            }
        }

        return new HtmlElement(tagName, parent, attributes);
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public String toString() {
        return name;
    }
}

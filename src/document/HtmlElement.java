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
        String[] parts = tag.split(" ");

        String tagName = parts[0].toLowerCase();
        HashMap<String, String> attributes = new HashMap<>();

        for (int i = 1; i < parts.length; i++) {
            String attrPair = parts[i];

            if (attrPair.contains("=")) {
                String[] attrs = attrPair.split("=", 2);
                String value = attrs[1];

                if (value.contains("'") || value.contains("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                attributes.put(attrs[0].toLowerCase(), value);
            }
            else {
                attributes.put(attrPair.toLowerCase(), "");
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

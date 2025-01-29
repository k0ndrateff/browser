package document;

import rendering.styles.CssBlock;
import rendering.styles.CssParser;
import rendering.styles.CssRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HtmlElement extends HtmlNode {
    private final String name;
    private final HashMap<String, String> attributes;

    private Map<String, CssRule> style;

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

        if (isReadingAttributes) {
            attributes.put(attributeName, token.toString());
        }
        else {
            tagName = token.toString().toLowerCase();
        }

        return new HtmlElement(tagName, parent, attributes);
    }

    public static String getTagName(String tag) {
        return tag.split(" ", 2)[0];
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void calculateStyle(ArrayList<CssBlock> rules) {
        Map<String, CssRule> style = new HashMap<>();

        for (CssBlock cssBlock : rules) {
            if (!cssBlock.getSelector().matches(this)) continue;

            for (CssRule rule : cssBlock.getRules().values()) {
                style.put(rule.getProperty(), rule);
            }
        }

        if (this.getAttributes().containsKey("style")) {
            style = new CssParser(this.getAttributes().get("style")).body();
        }

        this.style = style;

        for (HtmlNode child : this.getChildren()) {
            if (child instanceof HtmlElement) {
                ((HtmlElement) child).calculateStyle(rules);
            }
        }
    }

    public Map<String, CssRule> getStyle() {
        return style;
    }

    public String toString() {
        return name;
    }
}

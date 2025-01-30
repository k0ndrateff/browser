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

        for (Map.Entry<String, String> entry : HtmlElement.getInheritableStyleProperties().entrySet()) {
            if (this.parent != null && this.parent instanceof HtmlElement) {
                style.put(entry.getKey(), ((HtmlElement) this.parent).getStyle().get(entry.getKey()));
            }
            else {
                style.put(entry.getKey(), new CssRule(entry.getKey(), entry.getValue()));
            }
        }

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

        if (style.containsKey("font-size") && style.get("font-size") != null && style.get("font-size").getValue().endsWith("%")) {
            String parentFontSize;

            if (this.parent != null && this.parent instanceof HtmlElement) {
                parentFontSize = ((HtmlElement) this.parent).getStyle().get("font-size").getValue();
            }
            else {
                parentFontSize = HtmlElement.getInheritableStyleProperties().get("font-size");
            }

            float nodePercent = Float.parseFloat(this.style.get("font-size").getValue().replace("%", "")) / 100;
            float parentPx = Float.parseFloat(parentFontSize.replace("px", ""));
            this.style.put("font-size", new CssRule ("font-size", (nodePercent * parentPx) + "px"));
        }

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

    private static Map<String, String> getInheritableStyleProperties() {
        Map<String, String> props = new HashMap<>();

        props.put("font-size", "16px");
        props.put("font-style", "normal");
        props.put("font-weight", "normal");
        props.put("font-family", "sans-serif");
        props.put("color", "black");

        return props;
    }
}

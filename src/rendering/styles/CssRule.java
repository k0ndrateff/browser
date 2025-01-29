package rendering.styles;

public class CssRule {
    private final String property;
    private final String value;

    public CssRule(String property, String value) {
        this.property = property.toLowerCase();
        this.value = value;
    }

    public String getProperty() {
        return property;
    }

    public String getValue() {
        return value;
    }
}

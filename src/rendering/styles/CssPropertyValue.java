package rendering.styles;

public class CssPropertyValue {
    private final String property;
    private final String value;

    public CssPropertyValue(String property, String value) {
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

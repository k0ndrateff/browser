package document;

public class HtmlTag extends HtmlLayoutEntity {
    private final String name;

    public HtmlTag(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}

package document;

public class HtmlText extends HtmlLayoutEntity {
    private final String text;

    public HtmlText(String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }
}

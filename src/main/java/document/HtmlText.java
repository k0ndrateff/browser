package document;

public class HtmlText extends HtmlNode {
    private final String text;

    public HtmlText(String text, HtmlNode parent) {
        super(parent);

        this.text = text;
    }

    @Override
    public void appendChildren(HtmlNode node) {
        throw new IllegalStateException("Cannot append children to HTML text node.");
    }

    public String toString() {
        return text;
    }
}

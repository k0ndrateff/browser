package rendering.styles;

import document.HtmlElement;
import document.HtmlNode;

public class TagSelector extends Selector {
    private final String tag;

    public TagSelector(String tag) {
        this.tag = tag;
        this.priority = 1;
    }

    @Override
    public boolean matches(HtmlNode node) {
        return node instanceof HtmlElement && node.toString().equals(tag);
    }
}

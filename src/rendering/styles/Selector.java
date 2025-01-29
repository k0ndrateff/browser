package rendering.styles;

import document.HtmlNode;

public abstract class Selector {
    public abstract boolean matches(HtmlNode node);
}

package rendering.styles;

import document.HtmlNode;

public abstract class Selector {
    protected int priority;

    public int getPriority() {
        return priority;
    }

    public abstract boolean matches(HtmlNode node);
}

package rendering.styles.selector;

import document.HtmlNode;

public abstract class Selector {
    protected int priority;

    public int getPriority() {
        return priority;
    }

    public abstract boolean matches(HtmlNode node);
}

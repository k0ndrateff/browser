package rendering.styles.selector;

import document.HtmlNode;

public abstract class Selector {
    protected int priority;

    public int getPriority() {
        return priority;
    }

    public Selector setPriority(int priority) {
        this.priority = priority;

        return this;
    }

    public abstract boolean matches(HtmlNode node);
}

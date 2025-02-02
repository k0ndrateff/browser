package rendering.styles.selector;

import document.HtmlNode;

public class DescendantSelector extends Selector {
    private final Selector ancestor;
    private final Selector descendant;

    public DescendantSelector(Selector ancestor, Selector descendant) {
        this.ancestor = ancestor;
        this.descendant = descendant;

        this.priority = ancestor.getPriority() + descendant.getPriority();
    }

    @Override
    public boolean matches(HtmlNode node) {
        if (!descendant.matches(node)) return false;

        while (node.getParent() != null) {
            if (ancestor.matches(node.getParent())) return true;

            node = node.getParent();
        }

        return false;
    }
}

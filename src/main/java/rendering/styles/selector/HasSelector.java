package rendering.styles.selector;

import document.HtmlNode;

public class HasSelector extends Selector {
    private final Selector relativeSelector;

    public HasSelector(Selector relativeSelector) {
        this.relativeSelector = relativeSelector;

        this.priority = relativeSelector.getPriority();
    }

    @Override
    public boolean matches(HtmlNode node) {
        return node.getChildren().stream().anyMatch(relativeSelector::matches);
    }
}

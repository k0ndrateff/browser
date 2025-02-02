package rendering.styles.selector;

import document.HtmlNode;

import java.util.Arrays;

public class SelectorSequence extends Selector {
    private final Selector[] selectors;

    public SelectorSequence(Selector[] selectors) {
        this.selectors = selectors;

        this.priority = calculatePriority();
    }

    private int calculatePriority() {
        int priority = 0;

        for (Selector selector : selectors) {
            priority += selector.getPriority();
        }

        return priority;
    }


    @Override
    public boolean matches(HtmlNode node) {
        return Arrays.stream(selectors).allMatch(selector -> selector.matches(node));
    }
}

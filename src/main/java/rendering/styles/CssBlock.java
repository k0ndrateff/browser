package rendering.styles;

import rendering.styles.selector.Selector;

import java.util.Map;

public class CssBlock implements Comparable<CssBlock> {
    private final Selector selector;
    private final Map<String, CssRule> rules;

    public CssBlock(Selector selector, Map<String, CssRule> rules) {
        this.selector = selector;
        this.rules = rules;
    }

    public Selector getSelector() {
        return selector;
    }

    public Map<String, CssRule> getRules() {
        return rules;
    }

    @Override
    public int compareTo(CssBlock o) {
        return selector.getPriority() - o.getSelector().getPriority();
    }
}

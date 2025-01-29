package rendering.styles;

import java.util.Map;

public class CssBlock {
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
}

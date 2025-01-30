package rendering.styles;

import document.HtmlElement;
import document.HtmlNode;

import java.util.Arrays;

public class ClassSelector extends Selector {
    private final String className;

    public ClassSelector(String className) {
        this.className = className;

        this.priority = 10;
    }

    @Override
    public boolean matches(HtmlNode node) {
        if (!(node instanceof HtmlElement) || !((HtmlElement) node).getAttributes().containsKey("class")) return false;

        String[] classNames = ((HtmlElement) node).getAttributes().get("class").split(" ");

        return Arrays.stream(classNames).map(s -> s.substring(1)).anyMatch(c -> c.equals(className));
    }
}

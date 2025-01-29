package document;

import java.util.ArrayList;

public abstract class HtmlNode {
    protected final ArrayList<HtmlNode> children;
    protected final HtmlNode parent;

    public HtmlNode(HtmlNode parent) {
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public void appendChildren(HtmlNode node) {
        children.add(node);
    }

    public ArrayList<HtmlNode> getChildren() {
        return children;
    }

    public HtmlNode getParent() {
        return parent;
    }

    public abstract String toString();
}

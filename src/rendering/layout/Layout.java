package rendering.layout;

import document.HtmlNode;
import rendering.component.RenderingComponent;
import rendering.RenderingContext;

import java.util.ArrayList;
import java.util.Deque;

public abstract class Layout {
    protected HtmlNode node;
    protected Layout parent;
    protected Layout previous;
    protected ArrayList<Layout> children;

    protected RenderingContext ctx;

    protected int x;
    protected int y;
    protected int width;
    protected int height;

    protected Layout(HtmlNode node, RenderingContext ctx) {
        this.ctx = ctx;
        this.node = node;
        this.parent = null;
        this.previous = null;
        this.children = new ArrayList<>();
    }

    public Layout(HtmlNode node, Layout parent, Layout previous, RenderingContext ctx) {
        this.ctx = ctx;
        this.node = node;
        this.parent = parent;
        this.previous = previous;
        this.children = new ArrayList<>();
    }

    public int getHeight() {
        return height;
    }

    public ArrayList<Layout> getChildren() {
        return children;
    }

    public abstract Deque<RenderingComponent> getDisplayList();
    public abstract void render();
}

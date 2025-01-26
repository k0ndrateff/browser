package rendering.layout;

import document.HtmlNode;
import rendering.RenderingContext;
import rendering.TextRenderer;

import java.util.ArrayList;

public abstract class Layout {
    protected HtmlNode node;
    protected Layout parent;
    protected Layout previous;
    protected ArrayList<Layout> children;

    protected RenderingContext ctx;

    protected Layout(HtmlNode node, RenderingContext ctx) {
        this.ctx = ctx;
        this.node = node;
        this.parent = null;
        this.previous = null;
        this.children = new ArrayList<Layout>();
    }

    public Layout(HtmlNode node, Layout parent, Layout previous, RenderingContext ctx) {
        this.ctx = ctx;
        this.node = node;
        this.parent = parent;
        this.previous = previous;
        this.children = new ArrayList<>();
    }

    public abstract TextRenderer render();
}

package rendering.layout;

import document.HtmlNode;
import rendering.component.RenderingComponent;
import rendering.RenderingContext;

import java.util.ArrayDeque;
import java.util.Deque;

public class DocumentLayout extends Layout {
    public DocumentLayout(HtmlNode node, RenderingContext ctx) {
        super(node, ctx);
    }

    @Override
    public void render() {
        this.x = ctx.getPosition().x;
        this.y = ctx.getPosition().y;
        this.width = ctx.getWidth() - 2 * this.x;

        Layout child = new BlockLayout(this.node, this, null, ctx);
        this.children.add(child);
        child.render();

        this.height = child.height;
    }

    @Override
    public Deque<RenderingComponent> getDisplayList() {
        return new ArrayDeque<>();
    }
}

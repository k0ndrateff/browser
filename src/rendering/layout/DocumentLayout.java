package rendering.layout;

import document.HtmlNode;
import rendering.RenderingContext;
import rendering.TextRenderer;

public class DocumentLayout extends Layout {
    public DocumentLayout(HtmlNode node, RenderingContext ctx) {
        super(node, ctx);
    }

    @Override
    public TextRenderer render() {
        Layout child = new BlockLayout(this.node, this, null, ctx);
        this.children.add(child);
        return child.render();
    }
}

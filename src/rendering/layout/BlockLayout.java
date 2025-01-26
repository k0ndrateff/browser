package rendering.layout;

import document.HtmlNode;
import rendering.RenderingContext;
import rendering.TextRenderer;

public class BlockLayout extends Layout {
    public BlockLayout(HtmlNode node, Layout parent, Layout previous, RenderingContext ctx) {
        super(node, parent, previous, ctx);
    }

    @Override
    public TextRenderer render() {
        TextRenderer renderer = new TextRenderer(ctx);
        renderer.render(node);
        return renderer;
    }
}

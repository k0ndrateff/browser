package rendering;

import java.awt.*;

public class Text extends RenderingComponent {
    private final Font font;
    private final String text;

    public Text(String text, Point position, Font font) {
        this.text = text;
        this.position = position;
        this.font = font;
    }

    public void paint(Graphics g, PaintingContext ctx) {
        g.setColor(Color.BLACK);
        g.setFont(font);
        g.drawString(text, position.x, position.y - ctx.getScrollY());
    }
}

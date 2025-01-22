package rendering;

import java.awt.*;
import java.awt.font.LineMetrics;

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

    public Font getFont() {
        return font;
    }

    public LineMetrics getFontMetrics() {
        return font.getLineMetrics(text, TextRenderer.FRC);
    }

    public void incrementXPosition(int increment) {
        position.x += increment;
    }

    public void setPositionY(int positionY) {
        position.y = positionY;
    }

    @Override
    public String toString() {
        return text;
    }
}

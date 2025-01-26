package rendering.component;

import rendering.PaintingContext;
import rendering.TextRenderingProperty;
import rendering.layout.BlockLayout;

import java.awt.*;
import java.awt.font.LineMetrics;

public class Text extends RenderingComponent {
    private final Font font;
    private final String text;
    private final TextRenderingProperty property;

    public Text(String text, Point position, Font font, TextRenderingProperty property) {
        this.text = text;
        this.position = position;
        this.font = font;
        this.property = property;
    }

    public void paint(Graphics g, PaintingContext ctx) {
        g.setColor(Color.BLACK);
        g.setFont(font);
        g.drawString(text, position.x, position.y - ctx.getScrollY());
    }

    public Font getFont() {
        return font;
    }

    public TextRenderingProperty getProperty() {
        return property;
    }

    public LineMetrics getFontMetrics() {
        return font.getLineMetrics(text, BlockLayout.FRC);
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

package rendering;

import java.awt.*;

public class Character extends RenderingComponent {
    private final String character;
    private int scrollY;

    public Character(String character, Point position) {
        this.character = character;
        this.position = position;
    }

    public void paint(Graphics g, int scrollY) {
        this.scrollY = scrollY;
        this.paintComponent(g);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.drawString(character, position.x, position.y - scrollY);
    }
}

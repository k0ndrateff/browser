package rendering;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Queue;

public class PageCanvas extends JComponent implements KeyListener, MouseWheelListener {
    private static final int SCROLL_SPEED = 20;

    Queue<RenderingComponent> displayList;

    private int scrollY = 0;

    public PageCanvas() {
        setSize(800, 600);
        setFocusable(true);
        addKeyListener(this);
        addMouseWheelListener(this);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (displayList != null) {
            for (RenderingComponent component : displayList) {
                if (component.getPosition().y > scrollY + 600) continue;
                if (component.getPosition().y + 18 < scrollY) continue;

                component.paint(g, scrollY);
            }
        }
    }

    public void setText(Queue<RenderingComponent> displayList) {
        this.displayList = displayList;

        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            this.scrollY += SCROLL_SPEED;
            repaint();
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            this.scrollY -= SCROLL_SPEED;

            if (scrollY < 0) scrollY = 0;
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            this.scrollY += SCROLL_SPEED;
            repaint();
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            this.scrollY -= SCROLL_SPEED;

            if (scrollY < 0) scrollY = 0;
            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            scrollY += e.getWheelRotation() * SCROLL_SPEED;

            if (scrollY < 0) scrollY = 0;
            repaint();
        }
}

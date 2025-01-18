package rendering;

import error.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PageCanvas extends JComponent implements KeyListener, MouseWheelListener {
    private static final int SCROLL_SPEED = 20;

    DisplayList displayList;

    private int scrollY = 0;

    public PageCanvas() {
        setSize(800, 600);
        setFocusable(true);
        addKeyListener(this);
        addMouseWheelListener(this);
    }

    public void changeSize(int width, int height) {
        Logger.verbose("Browser window resized to " + width + "×" + height);

        setSize(width, height);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.clearRect(0, 0, getWidth(), getHeight());

        if (displayList != null) {
            for (RenderingComponent component : displayList.getDisplayList()) {
                if (component.getPosition().y > scrollY + getHeight()) continue;
                if (component.getPosition().y + 18 < scrollY) continue;

                component.paint(g, scrollY);
            }
        }
    }

    public void setText(DisplayList displayList) {
        this.displayList = displayList;

        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            this.scrollY += SCROLL_SPEED;

            if (scrollY > displayList.getLastEntryY()) scrollY = displayList.getLastEntryY();
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

            if (scrollY > displayList.getLastEntryY()) scrollY = displayList.getLastEntryY();
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
            if (scrollY > displayList.getLastEntryY()) scrollY = displayList.getLastEntryY();
            repaint();
        }
}

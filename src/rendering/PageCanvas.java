package rendering;

import error.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PageCanvas extends JComponent implements KeyListener, MouseWheelListener {
    private static final int SCROLL_SPEED = 20;
    JScrollBar scrollBar = new JScrollBar(JScrollBar.VERTICAL);
    private int scrollY = 0;

    DisplayList displayList;

    public PageCanvas() {
        setSize(800, 600);
        setFocusable(true);
        addKeyListener(this);
        addMouseWheelListener(this);

        this.setLayout(new BorderLayout());

        scrollBar.setUnitIncrement(SCROLL_SPEED);
        scrollBar.addAdjustmentListener(e -> {
            scrollY = e.getValue();
            repaint();
        });

        add(scrollBar, BorderLayout.EAST);
    }

    public int getDrawingWidth() {
        return getWidth() - scrollBar.getWidth();
    }

    public void changeSize(int width, int height) {
        Logger.verbose("Browser window resized to " + width + "×" + height);

        setSize(width, height);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.clearRect(0, 0, getDrawingWidth(), getHeight());

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
        scrollBar.setMaximum(displayList.getLastEntryY());

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

        scrollBar.setValue(scrollY);
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

        scrollBar.setValue(scrollY);
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        scrollY += e.getWheelRotation() * SCROLL_SPEED;

        if (scrollY < 0) scrollY = 0;
        if (scrollY > displayList.getLastEntryY()) scrollY = displayList.getLastEntryY();
        repaint();

        scrollBar.setValue(scrollY);
    }
}

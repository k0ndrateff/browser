package rendering;

import error.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PageCanvas extends JComponent implements KeyListener, MouseWheelListener {
    private static final int SCROLL_SPEED = 20;
    private int scrollY = 0;

    JScrollBar scrollBar = new JScrollBar(JScrollBar.VERTICAL);

    TextRenderer renderer;
    PaintingContext paintingContext = new PaintingContext(scrollY);

    public PageCanvas() {
        setSize(800, 600);
        setFocusable(true);
        addKeyListener(this);
        addMouseWheelListener(this);

        this.setLayout(new BorderLayout());

        scrollBar.setUnitIncrement(SCROLL_SPEED);
        scrollBar.addAdjustmentListener(e -> {
            scrollY = e.getValue();

            paintingContext.setScrollY(scrollY);
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

        if (renderer != null) {
            for (RenderingComponent component : renderer.getDisplayList()) {
                if (component.getPosition().y > scrollY + getHeight()) continue;
                if (component.getPosition().y + 18 < scrollY) continue;

                component.paint(g, paintingContext);
            }
        }
    }

    public void setText(TextRenderer displayList) {
        this.renderer = displayList;

        if (displayList.getLastDisplayListEntryY() > getHeight()) {
            scrollBar.setMaximum(getMaxScrollY());
        }
        else {
            scrollBar.setMaximum(0);
        }

        repaint();
    }

    private int getMaxScrollY() {
        return renderer.getLastDisplayListEntryY() - getHeight() + 20;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        handleKeyEvent(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        handleKeyEvent(e);
    }

    private void handleKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            this.scrollY += SCROLL_SPEED;

            if (scrollY > getMaxScrollY()) scrollY = getMaxScrollY();
            repaint();
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            this.scrollY -= SCROLL_SPEED;

            if (scrollY < 0) scrollY = 0;
            repaint();
        }

        paintingContext.setScrollY(scrollY);
        scrollBar.setValue(scrollY);
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        scrollY += e.getWheelRotation() * SCROLL_SPEED;

        if (scrollY < 0) scrollY = 0;
        if (scrollY > getMaxScrollY()) scrollY = getMaxScrollY();
        repaint();

        paintingContext.setScrollY(scrollY);
        scrollBar.setValue(scrollY);
    }
}

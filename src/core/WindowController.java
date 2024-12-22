package core;

import core.rendering.Character;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Objects;

public class WindowController extends JFrame implements KeyListener, MouseWheelListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    final static int HSTEP = 13;
    final static int VSTEP = 18;

    private int scroll = 0;
    private final LinkedList<Character> displayList = new LinkedList<>();

    private final DrawingCanvas canvas;

    // Custom canvas for drawing
    private static class DrawingCanvas extends JPanel {
        private final BufferedImage canvasImage;
        private final Graphics2D canvasGraphics;

        public DrawingCanvas(int width, int height) {
            canvasImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            canvasGraphics = canvasImage.createGraphics();

            canvasGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            canvasGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(canvasImage, 0, 0, null);
        }

        public void clearScreen() {
            canvasGraphics.setBackground(Color.WHITE);
            canvasGraphics.clearRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());
        }

        public void drawChar(String text, int x, int y) {
            canvasGraphics.setColor(Color.BLACK);
            canvasGraphics.drawString(text, x, y);
            repaint();
        }

        public void cleanup() {
            canvasGraphics.dispose();
        }
    }

    public WindowController() {
        setTitle("k0ndrateff/browser");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setFocusable(true);
        this.addKeyListener(this);
        this.addMouseWheelListener(this);

        this.canvas = new DrawingCanvas(WIDTH, HEIGHT);
        canvas.setBackground(Color.WHITE);
        add(canvas);

        setLocationRelativeTo(null);
    }

    public void createWindow() {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
        });
    }

    public void cleanup() {
        this.canvas.cleanup();
    }

    private void layout(String text) {
        int cursorX = HSTEP;
        int cursorY = VSTEP;

        for (int i = 0; i < text.length(); i++) {
            Character character = new Character(String.valueOf(text.charAt(i)), cursorX, cursorY);

            this.displayList.addLast(character);

            if (Objects.equals(character.c, "\n") || Objects.equals(character.c, "\r")) {
                cursorY += (int) (VSTEP * 1.5);
                cursorX = HSTEP;
            }
            else {
                cursorX += HSTEP;
            }

            if (cursorX >= this.getWidth() - HSTEP) {
                cursorX = HSTEP;
                cursorY += VSTEP;
            }
        }
    }

    private void draw() {
        this.canvas.clearScreen();

        for (Character character : this.displayList) {
            if (character.y > this.getHeight() + this.scroll) continue;
            if (character.y + VSTEP < this.scroll) continue;

            this.canvas.drawChar(character.c, character.x, character.y - this.scroll);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            this.scroll += 10;
            draw();
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            this.scroll -= 10;

            if (this.scroll < 0) this.scroll = 0;
            draw();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // No action required
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // No action required
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        this.scroll += e.getWheelRotation() * 10;

        if (this.scroll < 0) this.scroll = 0;
        draw();
    }

    public void drawPage(String content) {
        this.layout(content);

        this.draw();
    }
}

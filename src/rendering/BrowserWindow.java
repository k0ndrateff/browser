package rendering;

import core.Browser;
import document.HtmlDocument;
import document.HtmlElement;
import document.HtmlNode;
import document.HtmlParser;
import error.Logger;
import networking.request.Request;
import rendering.layout.DocumentLayout;
import rendering.styles.CssBlock;
import networking.*;
import rendering.styles.CssParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

public class BrowserWindow extends JFrame implements ComponentListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private final PageCanvas canvas;

    private HtmlParser htmlParser;
    private HtmlNode htmlTreeHead;
    private HtmlDocument htmlDocument;
    private ArrayList<String> stylesheetUrls;
    private final RenderingContext renderingContext;

    public BrowserWindow() {
        super("k0ndrateff/browser");

        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setFocusable(true);
        setResizable(true);
        setVisible(true);

        addComponentListener(this);

        canvas = new PageCanvas();
        add(canvas);

        this.renderingContext = new RenderingContext();
        renderingContext.setWidth(canvas.getDrawingWidth());
    }

    public void renderHtmlDocument(HtmlDocument document) {
        this.htmlDocument = document;
        this.htmlParser = HtmlParser.create(document);
        this.htmlTreeHead = htmlParser.parse();

        this.stylesheetUrls = HtmlParser.getStylesheetLinkUrls(htmlTreeHead);

        renderingContext.setPosition(document.isRtl() ? new Point(canvas.getDrawingWidth() - 40, 20) : new Point(20, 20));
        renderingContext.setIsRtl(document.isRtl());

        rerenderCurrentDocument();
    }

    private void rerenderCurrentDocument() {
        ArrayList<CssBlock> styles = Browser.BROWSER_STYLE_SHEET;

        for (String stylesheetUrl : stylesheetUrls) {
            URL url = htmlDocument.getRequestUrl().resolveRelativeURL(stylesheetUrl);
            String body = "";

            try {
                body = ((HtmlDocument) Request.create(url).make().getData()).getContent();
            }
            catch (Exception e) {
                Logger.error(e);
            }

            ArrayList<CssBlock> cssBlocks = new CssParser(body).parse();
            styles.addAll(cssBlocks);
        }

        if (htmlTreeHead instanceof HtmlElement) {
            ((HtmlElement) htmlTreeHead).calculateStyle(styles);
        }

        DocumentLayout layout = new DocumentLayout(htmlTreeHead, renderingContext);
        layout.render();
        canvas.setText(layout);

        repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (canvas != null) {
            canvas.changeSize(getWidth(), getHeight());
            renderingContext.setWidth(canvas.getDrawingWidth());

            rerenderCurrentDocument();
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentHidden(ComponentEvent e) {}
}

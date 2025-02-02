package rendering;

import core.Browser;
import document.HtmlDocument;
import document.HtmlElement;
import document.HtmlNode;
import document.HtmlParser;
import error.Logger;
import error.Performance;
import networking.request.Request;
import rendering.layout.DocumentLayout;
import rendering.styles.CssBlock;
import networking.*;
import rendering.styles.CssParser;
import rendering.styles.CssRule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.*;

public class BrowserWindow extends JFrame implements ComponentListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private final PageCanvas canvas;

    private HtmlParser htmlParser;
    private HtmlNode htmlTreeHead;
    private HtmlDocument htmlDocument;
    private ArrayList<CssBlock> styles;
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

        recalculateStyles();

        renderingContext.setPosition(document.isRtl() ? new Point(canvas.getDrawingWidth() - 40, 20) : new Point(20, 20));
        renderingContext.setIsRtl(document.isRtl());

        rerenderCurrentDocument();
    }

    private void rerenderCurrentDocument() {
        Performance.start("Rendering");
        DocumentLayout layout = new DocumentLayout(htmlTreeHead, renderingContext);
        layout.render();
        canvas.setText(layout);
        Performance.measure("Rendering");

        Performance.start("Painting");
        repaint();
        Performance.measure("Painting");
    }

    private void recalculateStyles() {
        Performance.start("Style recalculation");
        ArrayList<String> stylesheetUrls = HtmlParser.getStylesheetLinkUrls(htmlTreeHead);

        this.styles = Browser.BROWSER_STYLE_SHEET;

        HtmlElement headNode = (HtmlElement) htmlTreeHead.getChildren().getFirst();

        styles.addAll(getInlineStyles(headNode));

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

        this.styles = unpackShorthandStyles(styles);

        // sort by priorities (cascade style sheets)
        Collections.sort(styles);

        if (htmlTreeHead instanceof HtmlElement) {
            ((HtmlElement) htmlTreeHead).calculateStyle(styles);
        }

        Performance.measure("Style recalculation");
    }

    private ArrayList<CssBlock> unpackShorthandStyles(ArrayList<CssBlock> styles) {
        ArrayList<CssBlock> newStyles = new ArrayList<>();

        for (CssBlock cssBlock : styles) {
            Map<String, CssRule> newCssRules = new HashMap<>();

            for (Map.Entry<String, CssRule> rule : cssBlock.getRules().entrySet()) {
                if (Objects.equals(rule.getKey(), "font")) {
                    String[] fontStyles = rule.getValue().getValue().split(" ");

                    if (fontStyles.length < 4) continue;

                    String fontStyle = fontStyles[0];
                    String fontWeight = fontStyles[1];
                    String fontSize = fontStyles[2];
                    String fontFamily = fontStyles[3];

                    newCssRules.put("font-style", new CssRule("font-style", fontStyle));
                    newCssRules.put("font-weight", new CssRule("font-weight", fontWeight));
                    newCssRules.put("font-size", new CssRule("font-size", fontSize));
                    newCssRules.put("font-family", new CssRule("font-family", fontFamily));
                }
                else {
                    newCssRules.put(rule.getKey(), rule.getValue());
                }
            }

            CssBlock newBlock = new CssBlock(cssBlock.getSelector(), newCssRules);
            newStyles.add(newBlock);
        }

        return newStyles;
    }

    private ArrayList<CssBlock> getInlineStyles(HtmlElement headNode) {
        ArrayList<CssBlock> styles = new ArrayList<>();

        for (HtmlNode node : headNode.getChildren()) {
            if (node.toString().equals("style") && node.getChildren().size() == 1) {
                styles.addAll(new CssParser(node.getChildren().getFirst().toString()).parse());
            }
        }

        return styles;
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

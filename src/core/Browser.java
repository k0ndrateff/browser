package core;

import document.HtmlDocument;
import error.Logger;
import networking.request.Request;
import networking.URL;
import networking.request.http.HttpCache;
import rendering.BrowserWindow;
import rendering.styles.CssBlock;
import rendering.styles.CssParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Browser {
    public static final ArrayList<CssBlock> BROWSER_STYLE_SHEET = getBrowserStyleSheet();

    public static void main(String[] args) {
        HttpCache.init();
        BrowserWindow window = new BrowserWindow();

        URL url = new URL("http://example.org/");

        if (args.length > 0) {
            url = new URL(args[0]);

            Logger.verbose("Found URL in args: " + args[0]);
        }

        HtmlDocument response;

        try {
            response = (HtmlDocument) Request.create(url).make().getData();
        }
        catch (Exception e) {
            Logger.error(e);
            Logger.verbose("Request failed, redirecting to about:blank");
            response = (HtmlDocument) Request.create(URL.aboutBlank()).make().getData();
        }

        if (Arrays.asList(args).contains("--rtl")) {
            response.setRtl(true);
        }

        if (url.isViewSource()) {
            Logger.verbose("View-source scheme detected, printing HTML");
            response.setViewSource(true);
        }

        window.renderHtmlDocument(response);
    }

    private static ArrayList<CssBlock> getBrowserStyleSheet() {
        File file = new File("./resources/browser.css");
        StringBuilder result = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(file)) {
            result.append(new String(fis.readAllBytes(), StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            Logger.error(e);
            throw new RuntimeException("Unable to read browser.css file", e);
        }

        return new CssParser(result.toString()).parse();
    }
}

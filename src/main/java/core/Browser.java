package core;

import document.HtmlDocument;
import error.Logger;
import networking.request.Request;
import networking.URL;
import networking.request.http.HttpCache;
import rendering.BrowserWindow;
import rendering.styles.CssBlock;
import rendering.styles.CssParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Browser {
    public static final ArrayList<CssBlock> BROWSER_STYLE_SHEET = getBrowserStyleSheet();

    public static void main(String[] args) {
        HttpCache.init();
        BrowserWindow window = new BrowserWindow();

        URL url = new URL("https://browser.engineering/");

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

    public static String getResource(String fileName) throws IOException {
        InputStream inputStream = Browser.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + fileName);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private static ArrayList<CssBlock> getBrowserStyleSheet() {
        try {
            return new CssParser(getResource("browser.css")).parse();
        }
        catch (IOException e) {
            Logger.error(e);
            throw new RuntimeException("Unable to read browser.css file", e);
        }
    }
}

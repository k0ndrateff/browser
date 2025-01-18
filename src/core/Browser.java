package core;

import document.HtmlDocument;
import error.Logger;
import networking.request.Request;
import networking.URL;
import networking.request.http.HttpCache;
import rendering.BrowserWindow;

import java.util.Arrays;

public class Browser {
    public static void main(String[] args) {
        HttpCache.init();
        BrowserWindow window = new BrowserWindow();

        URL url = new URL("http://example.org/");
        boolean isRtl = false;

        if (args.length > 0) {
            url = new URL(args[0]);

            Logger.verbose("Found URL in args: " + args[0]);
        }

        if (Arrays.asList(args).contains("--rtl")) {
            isRtl = true;
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

        if (url.isViewSource()) {
            Logger.verbose("View-source scheme detected, printing HTML");

            window.displayText(response.getHtml(), isRtl);
        }
        else {
            window.displayText(response.getContent(), isRtl);
        }
    }
}

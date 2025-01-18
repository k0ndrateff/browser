package core;

import document.HtmlDocument;
import error.Logger;
import networking.request.Request;
import networking.URL;
import networking.request.http.HttpCache;
import rendering.BrowserWindow;

public class Browser {
    public static void main(String[] args) {
        HttpCache.init();
        BrowserWindow window = new BrowserWindow();

        URL url = new URL("http://example.org/");

        if (args.length > 0) {
            url = new URL(args[0]);

            Logger.verbose("Found URL in args: " + args[0]);
        }

        HtmlDocument response = (HtmlDocument) Request.create(url).make().getData();

        if (url.isViewSource()) {
            Logger.verbose("View-source scheme detected, printing HTML");

            window.displayText(response.getHtml());
        }
        else {
            window.displayText(response.getContent());
        }
    }
}

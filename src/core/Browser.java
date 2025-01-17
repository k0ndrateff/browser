package core;

import document.HtmlDocument;
import error.Logger;
import networking.request.Request;
import networking.URL;

public class Browser {
    public static void main(String[] args) {
        URL url = new URL("http://example.org/");

        if (args.length > 0) {
            url = new URL(args[0]);

            Logger.verbose("Found URL in args: " + args[0]);
        }

        HtmlDocument response = (HtmlDocument) Request.create(url).make().getData();

        if (url.isViewSource()) {
            Logger.verbose("View-source scheme detected, printing HTML");

            System.out.println(response.getHtml());
        }
        else {
            System.out.println(response.getContent());
        }
    }
}

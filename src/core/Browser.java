package core;

import document.HtmlDocument;
import request.Request;
import request.URL;

public class Browser {
    public static void main(String[] args) {
        URL url = new URL("http://example.org/");

        if (args.length > 0) {
            url = new URL(args[0]);
        }

        HtmlDocument response = (HtmlDocument) Request.create(url).make().getData();

        if (url.isViewSource()) {
            System.out.println(response.getHtml());
        }
        else {
            System.out.println(response.getContent());
        }
    }
}

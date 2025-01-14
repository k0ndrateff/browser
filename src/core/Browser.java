package core;

import request.Request;
import request.URL;

public class Browser {
    public static void main(String[] args) {
        URL url = new URL("http://example.org/");

        System.out.println(Request.create(url).make().getData());
    }
}

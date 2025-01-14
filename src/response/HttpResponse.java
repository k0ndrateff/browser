package response;

import error.NotImplementedException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HttpResponse extends Response {
    private String version;
    private String status;
    private String explanation;
    private Map<String, String> headers;
    private String body;

    public HttpResponse(String response) {
        super(response);

        this.processResponseHeaders(response);
    }

    private void processResponseHeaders(String response) {
        Map<String, String> headers = new HashMap<>();

        String[] responseParts = response.split("\r\n");
        String[] statusLineParts = responseParts[0].split(" ");

        version = statusLineParts[0];
        status = statusLineParts[1];
        explanation = statusLineParts[2];

        for (int i = 1; i < responseParts.length; i++) {
            String[] headerParts = responseParts[i].split(":");

            headers.put(headerParts[0].toLowerCase(Locale.ROOT), headerParts[1].toLowerCase(Locale.ROOT));
        }

        this.headers = headers;

        if (headers.containsKey("transfer-encoding") || headers.containsKey("content-encoding")) {
            throw new NotImplementedException("HTTP Response encoding");
        }
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String getData() {
        return body;
    }
}

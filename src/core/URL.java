package core;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class URL {
    private String scheme;
    private String host;
    private String path;

    public URL(String url) {
        String[] parts = url.split("://");

        this.scheme = parts[0];
        url = parts[1];

        assert Objects.equals(this.scheme, "http");

        if (!url.endsWith("/")) {
            url += "/";
        }

        String[] urlParts = url.split("/");

        this.host = urlParts[0];

        if (urlParts.length > 1) {
            url = urlParts[1];
            this.path = "/" + url;
        }
        else {
            this.path = "/";
        }
    }

    public String request() throws IOException {
        Socket socket = new Socket(this.host, 80);

        String req = "GET " + this.path + " HTTP/1.0\r\n";
        req += "Host: " + this.host + "\r\n";
        req += "\r\n";

        socket.getOutputStream().write(req.getBytes(StandardCharsets.UTF_8));

        String response = new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        String[] lines = response.split("\r\n");
        String[] statusLine = lines[0].split(" ");
        String version = statusLine[0];
        String status = statusLine[1];
        String explanation = statusLine[2];

        Map<String, String> responseHeaders = new HashMap<String, String>();
        boolean isReadingContent = false;
        String content = "";

        for (String line : lines) {
            if (line.startsWith("HTTP")) continue;

            if (isReadingContent) {
                content += line;

                continue;
            }

            if (line.isEmpty()) {
                isReadingContent = true;

                continue;
            }

            String[] headerSplit = line.split(":");
            String header = headerSplit[0];
            String value = headerSplit[1];

            responseHeaders.put(header.toLowerCase(), value.strip());
        }

        assert !responseHeaders.containsKey("transfer-encoding");
        assert !responseHeaders.containsKey("content-encoding");

        return content;
    }
}

package core;

import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class URL {
    private final String scheme;
    private String host;
    private String path;
    private short port;
    private String dataContent;

    public URL(String url) {
        String separator = "://";

        // Specific case – “data” scheme separates by colon
        if (url.startsWith("data")) {
            separator = ":";
        }

        String[] parts = url.split(separator, 2);

        this.scheme = parts[0];
        url = parts[1];

        // Currently only HTTP and HTTPS are supported
        String[] supportedSchemes = { "http", "https", "file", "data" };
        assert Arrays.asList(supportedSchemes).contains(this.scheme);

        switch (this.scheme) {
            case "http":
                this.port = 80;
                parseHttpUrl(url);
                break;

            case "https":
                this.port = 443;
                parseHttpUrl(url);
                break;

            case "file":
                parseFileUrl(url);
                break;

            case "data":
                parseDataUrl(url);
                break;
        }
    }

    private void parseHttpUrl(String url) {
        if (!url.endsWith("/")) {
            url += "/";
        }

        String[] urlParts = url.split("/", 2);

        this.host = urlParts[0];

        String[] hostParts = host.split(":", 2);

        if (hostParts.length > 1) {
            this.host = hostParts[0];
            this.port = Short.parseShort(hostParts[1]);
        }

        if (urlParts.length > 1) {
            url = urlParts[1];
            this.path = "/" + url;
        }
        else {
            this.path = "/";
        }
    }

    private void parseFileUrl(String url) {
        this.path = url;
    }

    private void parseDataUrl(String url) {
        String[] parts = url.split(",", 2);

        this.dataContent = parts[1];
    }

    private String getRequestMessage() {
        Map<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Host", this.host);
        defaultHeaders.put("Connection", "close");
        defaultHeaders.put("User-Agent", "k0ndrateff/browser");

        StringBuilder req = new StringBuilder("GET " + this.path + " HTTP/1.1\r\n");

        for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
            req.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        req.append("\r\n");

        return req.toString();
    }

    private String processHttpResponse(String response) {
        String[] lines = response.split("\r\n");
        String[] statusLine = lines[0].split(" ");
        String version = statusLine[0];
        String status = statusLine[1];
        String explanation = statusLine[2];

        Map<String, String> responseHeaders = new HashMap<>();
        boolean isReadingContent = false;
        StringBuilder content = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("HTTP")) continue;

            if (isReadingContent) {
                content.append(line);

                continue;
            }

            if (line.isEmpty()) {
                isReadingContent = true;

                continue;
            }

            String[] headerSplit = line.split(":", 2);
            String header = headerSplit[0];
            String value = headerSplit[1];

            responseHeaders.put(header.toLowerCase(), value.strip());
        }

        assert !responseHeaders.containsKey("transfer-encoding");
        assert !responseHeaders.containsKey("content-encoding");

        return content.toString();
    }

    private String httpRequest() {
        try (Socket socket = new Socket(this.host, this.port)) {
            String req = this.getRequestMessage();

            socket.getOutputStream().write(req.getBytes(StandardCharsets.UTF_8));

            String response = new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            return this.processHttpResponse(response);
        }
        catch (IOException e) {
            System.err.println("Could not connect to " + this.host + ":" + this.port + " | " + e.getMessage());
        }

        return null;
    }

    private String httpsRequest() {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        try (Socket socket = sslSocketFactory.createSocket(this.host, this.port)) {
            String req = this.getRequestMessage();

            socket.getOutputStream().write(req.getBytes(StandardCharsets.UTF_8));

            String response = new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            return this.processHttpResponse(response);
        }
        catch (IOException e) {
            System.err.println("Could not connect to " + this.host + ":" + this.port + " | " + e.getMessage());
        }

        return null;
    }

    private String fileRequest() {
        File file = new File(this.path);
        StringBuilder result = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(file)) {
            result.append(new String(fis.readAllBytes(), StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            System.err.println("Could not open file " + this.path + " | " + e.getMessage());
        }

        return result.toString();
    }

    private String dataRequest() {
        return this.dataContent;
    }

    public String request() throws IOException {
        return switch (this.scheme) {
            case "http" -> this.httpRequest();
            case "https" -> this.httpsRequest();
            case "file" -> this.fileRequest();
            case "data" -> this.dataRequest();
            default -> null;
        };
    }
}

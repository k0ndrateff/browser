package core;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class URL {
    private final String scheme;
    private String host;
    private String path;
    private short port;
    private String dataContent;
    private boolean isViewSource;

    private static final Map<String, Socket> socketPool = new ConcurrentHashMap<>();
    private static final Map<String, SSLSocket> sslSocketPool = new ConcurrentHashMap<>();

    public URL(String url) {
        if (url.startsWith("view-source:")) {
            isViewSource = true;
            url = url.replace("view-source:", "");
        }

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

    public boolean isViewSource() {
        return this.isViewSource;
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

        if (Objects.equals(this.scheme, "http")) {
            defaultHeaders.put("Connection", "Keep-Alive");
        }

        defaultHeaders.put("User-Agent", "k0ndrateff/browser");

        StringBuilder req = new StringBuilder("GET " + this.path + " HTTP/1.1\r\n");

        for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
            req.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        req.append("\r\n");

        return req.toString();
    }

    private String processHttpStatusLine(String response) {
        String[] lines = response.split("\r\n");

        String[] statusLine = lines[0].split(" ");
        String version = statusLine[0];
        String status = statusLine[1];
        String explanation = statusLine[2];

        return lines[0];
    }

    private Map<String, String> processResponseHeaders(String response) {
        String[] lines = response.split("\r\n");

        Map<String, String> responseHeaders = new HashMap<>();

        for (String line : lines) {
            if (line.startsWith("HTTP")) continue;
            if (line.isEmpty()) break;

            String[] headerSplit = line.split(":", 2);
            String header = headerSplit[0];
            String value = headerSplit[1];

            responseHeaders.put(header.toLowerCase(), value.strip());
        }

        assert !responseHeaders.containsKey("transfer-encoding");
        assert !responseHeaders.containsKey("content-encoding");

        return responseHeaders;
    }

    private String processHttpBody(String response) {
        String[] lines = response.split("\r\n");

        boolean isReadingContent = false;
        StringBuilder content = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("HTTP")) continue;

            if (line.isEmpty()) {
                isReadingContent = true;

                continue;
            }

            if (isReadingContent) content.append(line);
        }

        return content.toString();
    }

    private static void closeSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Failed to close socket: " + e.getMessage());
            }
        }
    }

    public static void closeAllSockets() {
        for (Map.Entry<String, Socket> entry : socketPool.entrySet()) {
            closeSocket(entry.getValue());
        }
        socketPool.clear();

        for (Map.Entry<String, SSLSocket> entry : sslSocketPool.entrySet()) {
            closeSocket(entry.getValue());
        }
        sslSocketPool.clear();
    }

    private String httpRequest() {
        String key = this.host + ":" + this.port;
        Socket socket = socketPool.get(key);

        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket(host, port);
                socket.setSoTimeout(5000);
                socketPool.put(key, socket);
            }

            String req = this.getRequestMessage();

            socket.getOutputStream().write(req.getBytes(StandardCharsets.UTF_8));

            String firstResponse = new String(socket.getInputStream().readNBytes(1000), StandardCharsets.UTF_8);
            Map<String, String> responseHeaders = processResponseHeaders(firstResponse);
            int contentLength = Integer.parseInt(responseHeaders.get("content-length"));
            int headersLength = firstResponse.split("\r\n\r\n")[0].length();

            if (contentLength > 1000) {
                String response = new String(socket.getInputStream().readNBytes(contentLength - (1000 - headersLength - 4)), StandardCharsets.UTF_8);
                return this.processHttpBody(firstResponse + response);
            }
            else {
                return this.processHttpBody(firstResponse);
            }
        }
        catch (IOException e) {
            System.err.println("Could not connect to " + this.host + ":" + this.port + " | " + e.getMessage());

            closeSocket(socket);
            socketPool.remove(key);
        }

        return null;
    }

    private String httpsRequest() {
        String key = this.host + ":" + this.port;
        Socket socket = sslSocketPool.get(key);

        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        try {
            if (socket == null || socket.isClosed()) {
                socket = sslSocketFactory.createSocket(this.host, this.port);
                socket.setSoTimeout(5000);
                sslSocketPool.put(key, (SSLSocket) socket);
            }

            String req = this.getRequestMessage();

            socket.getOutputStream().write(req.getBytes(StandardCharsets.UTF_8));

            String firstResponse = new String(socket.getInputStream().readNBytes(1000), StandardCharsets.UTF_8);
            Map<String, String> responseHeaders = processResponseHeaders(firstResponse);
            int contentLength = Integer.parseInt(responseHeaders.get("content-length"));
            int headersLength = firstResponse.split("\r\n\r\n")[0].length();

            if (contentLength > 1000) {
                String response = new String(socket.getInputStream().readNBytes(contentLength - (1000 - headersLength - 4)), StandardCharsets.UTF_8);
                return this.processHttpBody(firstResponse + response);
            }
            else {
                return this.processHttpBody(firstResponse);
            }
        }
        catch (IOException e) {
            System.err.println("Could not connect to " + this.host + ":" + this.port + " | " + e.getMessage());

            closeSocket(socket);
            sslSocketPool.remove(key);
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

package core.request;

import core.Browser;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class HTTPRequest {
    private final URL url;
    private final short redirectTryCount;

    private static final short MAX_REDIRECT_TRIES = 20;

    private static final Map<String, Socket> socketPool = new ConcurrentHashMap<>();
    private static final Map<String, SSLSocket> sslSocketPool = new ConcurrentHashMap<>();

    public HTTPRequest(URL url) {
        this.url = url;
        this.redirectTryCount = 0;
    }

    public HTTPRequest(URL url, short redirectTryCount) {
        if (redirectTryCount >= MAX_REDIRECT_TRIES) {
            throw new RuntimeException("Maximum number of redirects exceeded.");
        }

        this.url = url;
        this.redirectTryCount = redirectTryCount;
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

    private String[] processHttpStatusLine(String response) {
        String[] lines = response.split("\r\n");

        String[] statusLine = lines[0].split(" ");
        String version = statusLine[0];
        String explanation = statusLine[2];

        return statusLine;
    }

    private String getResponseStatus(String response) {
        return this.processHttpStatusLine(response)[1];
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

        if (responseHeaders.containsKey("transfer-encoding") || responseHeaders.containsKey("content-encoding")) {
            throw new RuntimeException("HTTP header contains unsupported transfer encoding.");
        }

        return responseHeaders;
    }

    private String getRequestMessage() {
        Map<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Host", this.url.getHost());

        if (Objects.equals(this.url.getScheme(), "http")) {
            defaultHeaders.put("Connection", "Keep-Alive");
        }

        defaultHeaders.put("User-Agent", "k0ndrateff/browser");

        StringBuilder req = new StringBuilder("GET " + this.url.getPath() + " HTTP/1.1\r\n");

        for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
            req.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        req.append("\r\n");

        return req.toString();
    }

    public String httpRequest() {
        if (Browser.getCachingController().contains(this.url.getHost(), this.url.getPort(), this.url.getPath())) {
            return Browser.getCachingController().get(this.url.getHost(), this.url.getPort(), this.url.getPath());
        }

        String key = this.url.getHost() + ":" + this.url.getPort();
        Socket socket = socketPool.get(key);

        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket(this.url.getHost(), this.url.getPort());
                socket.setSoTimeout(5000);
                socketPool.put(key, socket);
            }

            String req = this.getRequestMessage();

            socket.getOutputStream().write(req.getBytes(StandardCharsets.UTF_8));

            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder headersBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                headersBuilder.append(line).append("\r\n");
            }

            String headers = headersBuilder.toString();
            String status = this.getResponseStatus(headers);
            Map<String, String> responseHeaders = processResponseHeaders(headers);

            // REDIRECT
            if (status.startsWith("3")) {
                String newLocation = responseHeaders.get("location");

                if (newLocation.startsWith("/")) {
                    this.url.setPath(newLocation);
                    return this.url.request((short) (this.redirectTryCount + 1));
                }
                else {
                    return new URL(newLocation).request((short) (this.redirectTryCount + 1));
                }
            }

            int contentLength = Integer.parseInt(responseHeaders.get("content-length"));
            if (contentLength < 0) {
                throw new IOException("Invalid or missing Content-Length header");
            }

            char[] bodyChars = new char[contentLength];
            int bytesRead = reader.read(bodyChars, 0, contentLength);
            if (bytesRead != contentLength) {
                throw new IOException("Failed to read the entire body");
            }

            String body = new String(bodyChars);

            Browser.getCachingController().put(this.url.getHost(), this.url.getPort(), this.url.getPath(), body);
            return body;
        }
        catch (IOException e) {
            System.err.println("Could not connect to " + this.url.getHost() + ":" + this.url.getPort() + " | " + e.getMessage());

            closeSocket(socket);
            socketPool.remove(key);
        }

        return null;
    }

    public String httpsRequest() {
        if (Browser.getCachingController().contains(this.url.getHost(), this.url.getPort(), this.url.getPath())) {
            return Browser.getCachingController().get(this.url.getHost(), this.url.getPort(), this.url.getPath());
        }

        String key = this.url.getHost() + ":" + this.url.getPort();
        Socket socket = sslSocketPool.get(key);

        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        try {
            if (socket == null || socket.isClosed()) {
                socket = sslSocketFactory.createSocket(this.url.getHost(), this.url.getPort());
                socket.setSoTimeout(5000);
                sslSocketPool.put(key, (SSLSocket) socket);
            }

            String req = this.getRequestMessage();

            socket.getOutputStream().write(req.getBytes(StandardCharsets.UTF_8));

            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder headersBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                headersBuilder.append(line).append("\r\n");
            }

            String headers = headersBuilder.toString();
            String status = this.getResponseStatus(headers);
            Map<String, String> responseHeaders = processResponseHeaders(headers);

            // REDIRECT
            if (status.startsWith("3")) {
                String newLocation = responseHeaders.get("location");

                if (newLocation.startsWith("/")) {
                    this.url.setPath(newLocation);
                    return this.url.request((short) (this.redirectTryCount + 1));
                }
                else {
                    return new URL(newLocation).request((short) (this.redirectTryCount + 1));
                }
            }

            int contentLength = Integer.parseInt(responseHeaders.get("content-length"));
            if (contentLength < 0) {
                throw new IOException("Invalid or missing Content-Length header");
            }

            char[] bodyChars = new char[contentLength];
            int bytesRead = reader.read(bodyChars, 0, contentLength);
            if (bytesRead != contentLength) {
                throw new IOException("Failed to read the entire body");
            }

            String body = new String(bodyChars);

            Browser.getCachingController().put(this.url.getHost(), this.url.getPort(), this.url.getPath(), body);
            return body;
        }
        catch (IOException e) {
            System.err.println("Could not connect to " + this.url.getHost() + ":" + this.url.getPort() + " | " + e.getMessage());

            closeSocket(socket);
            sslSocketPool.remove(key);
        }

        return null;
    }
}

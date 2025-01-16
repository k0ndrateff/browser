package request;

import error.Logger;
import error.NotImplementedException;
import response.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest extends Request {
    private static final String DEFAULT_METHOD = "GET";
    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final int DEFAULT_PORT = 80;

    public HttpRequest(URL url) {
        super(url);
    }

    private Map<String, String> getRequestHeaders() {
        Map<String, String> headers = new HashMap<>();

        headers.put("Connection", "keep-alive");
        headers.put("User-Agent", "k0ndrateff/browser");

        return headers;
    }

    private String getRequestHeader() {
        Map<String, String> headers = getRequestHeaders();

        StringBuilder request = new StringBuilder(DEFAULT_METHOD + " " + url.getPath() + " " + HTTP_VERSION + "\r\n");
        request.append("Host: ").append(url.getHost()).append("\r\n");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            request.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        request.append("\r\n");

        return request.toString();
    }

    protected HttpResponse performRequestWithSocket(Socket socket) throws IOException {
        socket.getOutputStream().write(getRequestHeader().getBytes(StandardCharsets.UTF_8));

        InputStream inputStream = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        StringBuilder headersBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            headersBuilder.append(line).append("\r\n");
        }

        HttpResponse response = new HttpResponse(headersBuilder.toString());

        if (response.getStatus().startsWith("3") && response.getHeaders().containsKey("location")) {
            Logger.verbose("Redirecting to " + response.getHeaders().get("location"));

            return (HttpResponse) Request.create(this.url.getRedirectedUrl(response.getHeaders().get("location"))).make();
        }

        int contentLength = 0;

        if (response.getHeaders().containsKey("content-length")) {
            Logger.verbose("HTTP Content-Length: " + response.getHeaders().get("content-length"));

            contentLength = Integer.parseInt(response.getHeaders().get("content-length"));
        }
        else {
            throw new NotImplementedException("Handling HTTP response without Content-Length");
        }

        StringBuilder bodyBuilder = new StringBuilder();

        // TODO: fix this part not receiving full data sometimes
        char[] buffer = new char[contentLength];
        int bytesRead = reader.read(buffer, 0, contentLength);
        
        bodyBuilder.append(buffer, 0, bytesRead);

        response.setBody(bodyBuilder.toString());

        return response;
    }

    @Override
    public HttpResponse make() {
        Logger.verbose("Making HTTP request...");

        int port = DEFAULT_PORT;

        if (this.url.isPortDefined()) {
            port = this.url.getPort();
        }

        try (Socket socket = SocketPool.getSocket(this.url.getHost(), port)) {
            return this.performRequestWithSocket(socket);
        }
        catch (Exception e) {
            Logger.error(e);

            throw new NotImplementedException("Handling broken requests");
        }
    }
}

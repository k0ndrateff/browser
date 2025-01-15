package request;

import error.Logger;
import error.NotImplementedException;
import response.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HttpRequest extends Request {
    private static final int DEFAULT_PORT = 80;

    public HttpRequest(URL url) {
        super(url);
    }

    private String getRequestHeader() {
        String request = "GET " + url.getPath() + " HTTP/1.0\r\n";
        request += "Host: " + url.getHost() + "\r\n";
        request += "\r\n";

        return request;
    }

    protected HttpResponse performRequestWithSocket(Socket socket) throws IOException {
        socket.getOutputStream().write(getRequestHeader().getBytes(StandardCharsets.UTF_8));

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        StringBuilder headersBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            headersBuilder.append(line).append("\r\n");
        }

        HttpResponse response = new HttpResponse(headersBuilder.toString());

        StringBuilder bodyBuilder = new StringBuilder();

        // Does not work, implement reading by Content-Length
        int c;
        while ((c = reader.read()) != -1) {
            bodyBuilder.append((char) c);
        }
        response.setBody(bodyBuilder.toString());

        return response;
    }

    @Override
    public HttpResponse make() {
        int port = DEFAULT_PORT;

        if (this.url.isPortDefined()) {
            port = this.url.getPort();
        }

        try (Socket socket = new Socket(this.url.getHost(), port)) {
            return this.performRequestWithSocket(socket);
        }
        catch (Exception e) {
            Logger.error(e);

            throw new NotImplementedException("Handling broken requests");
        }
    }
}

package core.request;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class URL {
    private final String scheme;
    private String host;
    private String path;
    private short port;
    private String dataContent;
    private boolean isViewSource;

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

    public String getScheme() {
        return this.scheme;
    }

    public String getHost() {
        return this.host;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public short getPort() {
        return this.port;
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
            case "http" -> new HTTPRequest(this).httpRequest();
            case "https" -> new HTTPRequest(this).httpsRequest();
            case "file" -> this.fileRequest();
            case "data" -> this.dataRequest();
            default -> null;
        };
    }

    public String request(short redirectTryCount) throws IOException {
        return switch (this.scheme) {
            case "http" -> new HTTPRequest(this, redirectTryCount).httpRequest();
            case "https" -> new HTTPRequest(this, redirectTryCount).httpsRequest();
            default -> null;
        };
    }
}

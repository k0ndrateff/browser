package request;

public class URL {
    private final String url;

    private final UrlScheme scheme;
    private final String host;
    private final String path;

    public URL(String url) {
        this.url = url;

        String[] parts = url.split("://", 2);
        this.scheme = UrlScheme.fromIdentifier(parts[0]);
        String restUrl = parts[1];

        if (!restUrl.contains("/")) {
            restUrl += "/";
        }

        String[] hostParts = restUrl.split("/", 2);
        this.host = hostParts[0];
        this.path = "/" + hostParts[1];
    }

    public UrlScheme getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }
}

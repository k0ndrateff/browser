package networking;

public class URL {
    private final String url;

    private final UrlScheme scheme;
    private String host;
    private int port;
    private String path;

    private boolean isViewSource = false;

    public URL(String url) {
        this.url = url;

        if (url.startsWith("view-source:")) {
            this.isViewSource = true;

            url = url.substring(12);
        }

        if (url.startsWith(UrlScheme.DATA.identifier)) {
            this.scheme = UrlScheme.DATA;

            parseDataUrl(url.split(":", 2)[1]);

            return;
        }

        String[] parts = url.split("://", 2);
        this.scheme = UrlScheme.fromIdentifier(parts[0]);
        String restUrl = parts[1];

        if (!restUrl.contains("/")) {
            restUrl += "/";
        }

        if (scheme.equals(UrlScheme.HTTP) || scheme.equals(UrlScheme.HTTPS)) {
            parseHttpUrl(restUrl);
        }
        else if (scheme.equals(UrlScheme.FILE)) {
            parseFileUrl(restUrl);
        }
    }

    private URL(UrlScheme scheme, String host, int port, String path) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.path = path;

        this.url = this.constructUrlString(scheme, host, port, path);
    }

    public URL getRedirectedUrl(String redirectURL) {
        if (redirectURL.startsWith("/")) {
            return new URL(scheme, host, port, redirectURL).setViewSource(isViewSource);
        }
        else {
            return new URL(redirectURL).setViewSource(isViewSource);
        }
    }

    private void parseHttpUrl(String url) {
        String[] hostParts = url.split("/", 2);
        String host = hostParts[0];
        this.path = "/" + hostParts[1];

        if (host.contains(":")) {
            String[] hostPort = host.split(":", 2);
            this.host = hostPort[0];
            this.port = Integer.parseInt(hostPort[1]);
        }
        else {
            this.host = host;
            this.port = -1;
        }
    }

    private void parseFileUrl(String url) {
        this.path = url;
    }

    private void parseDataUrl(String url) {
        this.path = url;
    }

    private String constructUrlString(UrlScheme scheme, String host, int port, String path) {
        if (port != -1) {
            return scheme + "://" + host + ":" + port + path;
        }
        else {
            return scheme + "://" + host + path;
        }
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

    public int getPort() {
        return port;
    }

    public boolean isPortDefined() {
        return port != -1;
    }

    public boolean isViewSource() {
        return isViewSource;
    }

    private URL setViewSource(boolean isViewSource) {
        this.isViewSource = isViewSource;

        return this;
    }
}

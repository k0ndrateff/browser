package networking;

import error.NotImplementedException;

public enum UrlScheme {
    HTTP("http"),
    HTTPS("https"),
    FILE("file"),
    DATA("data");

    final String identifier;

    UrlScheme(final String scheme) {
        this.identifier = scheme;
    }

    public static UrlScheme fromIdentifier(final String identifier) {
        for (final UrlScheme scheme : UrlScheme.values()) {
            if (scheme.identifier.equalsIgnoreCase(identifier)) {
                return scheme;
            }
        }

        throw new NotImplementedException("URL Scheme " + identifier);
    }
}

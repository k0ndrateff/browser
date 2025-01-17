package networking.request.http;

import java.time.Instant;
import java.util.UUID;

public class HttpCacheEntry {
    private final UUID uuid;
    private final String url;
    private final Instant freshUntil;

    public HttpCacheEntry(String url, Instant freshUntil) {
        this.uuid = UUID.randomUUID();
        this.url = url;
        this.freshUntil = freshUntil;
    }

    private HttpCacheEntry(UUID uuid, String url, Instant freshUntil) {
        this.uuid = uuid;
        this.url = url;
        this.freshUntil = freshUntil;
    }

    public static HttpCacheEntry parseFromString(String cacheString) {
        String[] parts = cacheString.split(" ");

        return new HttpCacheEntry(UUID.fromString(parts[0]), parts[1], Instant.parse(parts[2]));
    }

    @Override
    public String toString() {
        return this.uuid.toString() + " " + this.url + " " + this.freshUntil.toString();
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean isFresh() {
        return Instant.now().isBefore(this.freshUntil);
    }
}

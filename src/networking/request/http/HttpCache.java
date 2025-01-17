package networking.request.http;

import error.Logger;
import networking.response.HttpResponse;
import networking.URL;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpCache {
    public static final String CACHE_DIR = "cache";
    public static final String CACHE_DB = "cache.db";
    
    private static final Map<String, HttpCacheEntry> cache = new HashMap<>();
    
    public static void init() {
        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        
        File cacheFile = new File(cacheDir, CACHE_DB);
        if (!cacheFile.exists()) {
            Logger.verbose("HTTP Cache file not found, creating new one...");

            try {
                cacheFile.createNewFile();
            } catch (IOException e) {
                Logger.error(e);
            }
        }
        else {
            Logger.verbose("HTTP Cache file found, loading entries...");

            try (BufferedReader reader = new BufferedReader(new FileReader(cacheFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    HttpCacheEntry entry = HttpCacheEntry.parseFromString(line);

                    cache.put(entry.getUrl(), entry);
                }
            } catch (IOException e) {
                Logger.error(e);
            }
        }

        clearDeadCache();
    }

    private static void clearDeadCache() {
        for (HttpCacheEntry entry : cache.values()) {
            if (!entry.isFresh()) {
                File contentFile = new File(CACHE_DIR, entry.getUuid().toString());
                contentFile.delete();

                cache.remove(entry.getUrl());
            }
        }
    }

    private static boolean shouldResponseBeCached(HttpResponse response) {
        if (!response.getStatus().equals("200")) return false;

        Map<String, String> headers = response.getHeaders();

        if (headers.containsKey("cache-control")) {
            String cacheControl = headers.get("cache-control");

            if (cacheControl.equals("no-store")) return false;

            return cacheControl.contains("max-age");
        }

        return true;
    }
    
    private static void createContentFile(UUID uuid, String content) {
        try {
            File cacheFile = new File(CACHE_DIR, uuid.toString());

            Files.writeString(cacheFile.toPath(), content);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private static void updateCacheDb() {
        try {
            File cacheDbFile = new File(CACHE_DIR, CACHE_DB);
    
            StringBuilder cacheData = new StringBuilder();
            for (HttpCacheEntry entry : cache.values()) {
                cacheData.append(entry.toString()).append(System.lineSeparator());
            }
    
            Files.writeString(cacheDbFile.toPath(), cacheData.toString());
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    public static void store(HttpResponse response) {
        if (!shouldResponseBeCached(response)) return;

        String url = response.getUrl().getCanonicalUrl();
        Map<String, String> headers = response.getHeaders();

        Logger.verbose("Storing " + url + " in HTTP cache...");
        
        Pattern maxAgePattern = Pattern.compile("max-age=(\\d+)");
        int maxAge = 86400;

        if (headers.containsKey("cache-control")) {
            String cacheControl = headers.get("cache-control");
            Matcher maxAgeMatcher = maxAgePattern.matcher(cacheControl);

            if (maxAgeMatcher.find()) {
               maxAge = Integer.parseInt(maxAgeMatcher.group(1));
            }
        }
        
        Instant freshUntil = Instant.now().plusSeconds(maxAge);
        HttpCacheEntry entry = new HttpCacheEntry(url, freshUntil);
        cache.put(url, entry);
        createContentFile(entry.getUuid(), response.getData().getHtml());
        updateCacheDb();
    }

    public static HttpResponse retrieve(URL url) {
        if (!cache.containsKey(url.getCanonicalUrl())) return null;
        
        HttpCacheEntry entry = cache.get(url.getCanonicalUrl());
        
        if (!entry.isFresh()) return null;
        
        String uuid = entry.getUuid().toString();
        
        try {
            File contentFile = new File(CACHE_DIR, uuid);
            String htmlContent = Files.readString(contentFile.toPath());

            HttpResponse response = new HttpResponse(url);
            response.setBody(htmlContent);

            Logger.verbose("Retrieved " + url.getCanonicalUrl() + " from HTTP cache");
            return response;
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }
    }
}

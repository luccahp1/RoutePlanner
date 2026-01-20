package com.onlyeavestroughs.routeplanner.ors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyeavestroughs.routeplanner.util.HashUtil;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Forward geocoding using openrouteservice public API.
 *
 * Uses Authorization header for API key (preferred by ORS docs).
 * Returns best match coordinates (lng, lat) from features[0].
 */
public final class OrsGeocoder {

    private static final String BASE_URL = "https://api.openrouteservice.org";

    private final String apiKey;
    private final Path cacheDir;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public OrsGeocoder(String apiKey, Path cacheDir) {
        this.apiKey = apiKey;
        this.cacheDir = cacheDir;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    public GeocodeOutcome forwardGeocode(String address) throws Exception {
        String normalized = address == null ? "" : address.trim();
        if (normalized.isEmpty()) {
            return GeocodeOutcome.fail(address, "blank address", false);
        }

        Files.createDirectories(cacheDir);
        Path cacheFile = cacheDir.resolve(HashUtil.sha1Hex(normalized) + ".json");

        // Cache hit
        if (Files.exists(cacheFile)) {
            try {
                JsonNode cached = mapper.readTree(cacheFile.toFile());
                if (cached.hasNonNull("lat") && cached.hasNonNull("lng")) {
                    double lat = cached.get("lat").asDouble();
                    double lng = cached.get("lng").asDouble();
                    return GeocodeOutcome.ok(address, lat, lng, true, "cache");
                }
            } catch (Exception ignored) {
                // Corrupted cache -> fall back to API and overwrite it.
            }
        }

        // API call with basic retry/backoff
        int maxAttempts = 5;
        long backoffMs = 400;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            HttpResponse<String> resp;
            try {
                resp = http.send(buildRequest(normalized), HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    return GeocodeOutcome.fail(address, "HTTP error: " + e.getMessage(), false);
                }
                Thread.sleep(backoffMs);
                backoffMs *= 2;
                continue;
            }

            int code = resp.statusCode();
            if (code == 200) {
                return parseAndCache(address, resp.body(), cacheFile);
            }

            // Retry throttling / transient server errors
            if (code == 429 || code == 502 || code == 503 || code == 504) {
                if (attempt == maxAttempts) {
                    return GeocodeOutcome.fail(address, "ORS error HTTP " + code, false);
                }
                Thread.sleep(backoffMs);
                backoffMs *= 2;
                continue;
            }

            // Non-retryable
            return GeocodeOutcome.fail(address, "ORS error HTTP " + code + ": " + safeSnippet(resp.body()), false);
        }

        return GeocodeOutcome.fail(address, "unknown geocode failure", false);
    }

    private HttpRequest buildRequest(String address) {
        String text = URLEncoder.encode(address, StandardCharsets.UTF_8);

        // boundary.country=CA helps disambiguate within Canada.
        String url = BASE_URL + "/geocode/search?text=" + text + "&size=1&boundary.country=CA";

        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", apiKey)
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    private GeocodeOutcome parseAndCache(String originalAddress, String body, Path cacheFile) throws Exception {
        JsonNode root = mapper.readTree(body);
        JsonNode features = root.get("features");
        if (features == null || !features.isArray() || features.isEmpty()) {
            return GeocodeOutcome.fail(originalAddress, "no geocode results", false);
        }

        JsonNode first = features.get(0);
        JsonNode geom = first.get("geometry");
        JsonNode coords = geom == null ? null : geom.get("coordinates");
        if (coords == null || !coords.isArray() || coords.size() < 2) {
            return GeocodeOutcome.fail(originalAddress, "invalid geometry in response", false);
        }

        double lng = coords.get(0).asDouble();
        double lat = coords.get(1).asDouble();

        JsonNode cacheNode = mapper.createObjectNode()
                .put("address", originalAddress)
                .put("lat", lat)
                .put("lng", lng)
                .put("cachedAt", Instant.now().toString());

        mapper.writerWithDefaultPrettyPrinter().writeValue(cacheFile.toFile(), cacheNode);

        return GeocodeOutcome.ok(originalAddress, lat, lng, false, "api");
    }

    private static String safeSnippet(String body) {
        if (body == null) return "";
        String b = body.replaceAll("\n", " ").trim();
        return b.length() <= 200 ? b : b.substring(0, 200) + "...";
    }

    public record GeocodeOutcome(
            boolean success,
            String address,
            double lat,
            double lng,
            boolean fromCache,
            String message
    ) {
        public static GeocodeOutcome ok(String address, double lat, double lng, boolean fromCache, String msg) {
            return new GeocodeOutcome(true, address, lat, lng, fromCache, msg);
        }

        public static GeocodeOutcome fail(String address, String msg, boolean fromCache) {
            return new GeocodeOutcome(false, address, Double.NaN, Double.NaN, fromCache, msg);
        }
    }
}

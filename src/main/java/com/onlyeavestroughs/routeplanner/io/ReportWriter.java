package com.onlyeavestroughs.routeplanner.io;

import com.onlyeavestroughs.routeplanner.ors.OrsGeocoder.GeocodeOutcome;
import com.onlyeavestroughs.routeplanner.runtime.RunApp.Stop;
import com.onlyeavestroughs.routeplanner.runtime.RunConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ReportWriter {
    private ReportWriter() {}

    public static void writeRoutesTxt(Path file, RunConfig cfg, GeocodeOutcome depotGeo, List<Stop> stops) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("Route Planner - Milestone 2\n");
        sb.append("Run ID: ").append(cfg.runId()).append("\n");
        sb.append("Profile: ").append(cfg.profile()).append("\n");
        sb.append("Generated: ")
                .append(ZonedDateTime.now(ZoneId.of("America/Toronto"))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")))
                .append("\n\n");

        sb.append("Depot (start/end):\n");
        sb.append("- ").append(cfg.depotAddress()).append("\n");
        if (depotGeo != null && depotGeo.success()) {
            sb.append(String.format("  (%.6f, %.6f) [%s]\n",
                    depotGeo.lat(), depotGeo.lng(), depotGeo.fromCache() ? "cache" : "api"));
        }
        sb.append("\n");

        sb.append("Stops (geocoded): ").append(stops.size()).append("\n");
        for (Stop s : stops) {
            sb.append(String.format("%3d. %s\n     (%.6f, %.6f)\n",
                    s.id(), s.address(), s.lat(), s.lng()));
        }

        sb.append("\nNOTE: Optimization/matrix not implemented yet (Milestone 2).\n");

        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    public static void writeDebugReport(
            Path file,
            RunConfig cfg,
            AddressReader.ReadResult readResult,
            GeocodeOutcome depotGeo,
            List<String> stopsRaw,
            List<String> failedStops,
            int cacheHits,
            int apiHits
    ) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("Route Planner - Debug Report (Milestone 2)\n");
        sb.append("Run ID: ").append(cfg.runId()).append("\n");
        sb.append("Input: ").append(cfg.inputFile()).append("\n");
        sb.append("Profile: ").append(cfg.profile()).append("\n\n");

        sb.append("Input stats\n");
        sb.append("- Raw lines: ").append(readResult.rawLineCount()).append("\n");
        sb.append("- Blank lines removed: ").append(readResult.blankLineCount()).append("\n");
        sb.append("- Exact duplicates removed: ").append(readResult.duplicateLineCount()).append("\n");
        sb.append("- Cleaned lines: ").append(readResult.addresses().size()).append("\n");
        sb.append("- Stops after depot strip: ").append(stopsRaw.size()).append("\n\n");

        sb.append("Geocoding\n");
        if (depotGeo != null) {
            sb.append("- Depot: ").append(depotGeo.success() ? "OK" : "FAILED").append(" (\"").append(cfg.depotAddress()).append("\")\n");
            sb.append("  Details: ").append(depotGeo.message()).append("\n");
        }
        sb.append("- Cache hits: ").append(cacheHits).append("\n");
        sb.append("- API hits: ").append(apiHits).append("\n");
        sb.append("- Failed stops: ").append(failedStops.size()).append("\n\n");

        if (!failedStops.isEmpty()) {
            sb.append("Failed stop list:\n");
            for (String f : failedStops) {
                sb.append("- ").append(f).append("\n");
            }
            sb.append("\n");
        }

        sb.append("Notes\n");
        sb.append("- Milestone 2 geocodes depot + stops and caches results under cache/geocode/.\n");
        sb.append("- If a stop fails geocoding, it is skipped for now and reported above.\n");

        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }
}

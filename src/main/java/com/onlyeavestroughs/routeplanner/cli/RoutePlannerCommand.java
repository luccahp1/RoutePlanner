package com.onlyeavestroughs.routeplanner.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Command(
        name = "routeplanner",
        mixinStandardHelpOptions = true,
        version = "0.1.0",
        description = "Milestone 1: read addresses, create run folder, write initial output files."
)
public class RoutePlannerCommand {

    // Keep Picocli annotations for later (or if you still want CLI),
    // but we won't require them for IntelliJ ▶ runs because we set them programmatically.
    @Option(names = "--depot", description = "Depot/start address for this run (driver's home).", required = true)
    private String depot;

    @Option(names = {"-i", "--input"}, description = "Path to addresses TXT (one address per line).", required = true)
    private Path input;

    @Option(names = {"-o", "--out"}, description = "Output root directory (default: output).")
    private Path outRoot = Paths.get("output");

    @Option(names = "--cache", description = "Cache root directory (default: cache).")
    private Path cacheRoot = Paths.get("cache");

    // -------------------------
    // Setters for no-args runs
    // -------------------------
    public void setDepot(String depot) {
        this.depot = depot;
    }

    public void setInputPath(String inputPath) {
        this.input = Paths.get(inputPath);
    }

    public void setOutRoot(String outRoot) {
        this.outRoot = Paths.get(outRoot);
    }

    public void setCacheRoot(String cacheRoot) {
        this.cacheRoot = Paths.get(cacheRoot);
    }

    // -------------------------
    // Main entry for Milestone 1
    // -------------------------
    public int runMilestone1() throws Exception {
        validateRequired();

        // Ensure cache directory exists (future milestones will use it)
        Files.createDirectories(cacheRoot);

        // Create output run folder: output/YYYY-MM-DD_HH-mm-ss
        Files.createDirectories(outRoot);
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        Path runDir = outRoot.resolve(stamp);
        Files.createDirectories(runDir);

        // Read + normalize addresses
        List<String> rawLines = Files.readAllLines(input, StandardCharsets.UTF_8);

        int rawCount = rawLines.size();
        int blankRemoved = 0;

        List<String> cleaned = new ArrayList<>(rawLines.size());
        for (String line : rawLines) {
            if (line == null) {
                blankRemoved++;
                continue;
            }
            String t = line.trim();
            if (t.isEmpty()) {
                blankRemoved++;
                continue;
            }
            cleaned.add(t);
        }

        // Exact dedupe (preserve order)
        int beforeDedupe = cleaned.size();
        LinkedHashSet<String> set = new LinkedHashSet<>(cleaned);
        List<String> deduped = new ArrayList<>(set);
        int duplicatesRemoved = beforeDedupe - deduped.size();

        // Write routes.txt (human-readable)
        Path routesTxt = runDir.resolve("routes.txt");
        try (BufferedWriter w = Files.newBufferedWriter(routesTxt, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            w.write("RoutePlanner - Milestone 1\n");
            w.write("Run: " + stamp + "\n");
            w.write("Depot: " + depot + "\n");
            w.write("\n");
            w.write("Stops (" + deduped.size() + "):\n");
            for (int i = 0; i < deduped.size(); i++) {
                w.write(String.format("%3d) %s%n", i + 1, deduped.get(i)));
            }
            w.write("\n");
            w.write("NOTE: Optimization/geocoding/matrix not implemented yet (Milestone 1).\n");
        }

        // Write routes.json (skeleton for next milestones)
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("milestone", 1);
        json.put("runTimestamp", stamp);
        json.put("depot", depot);

        Map<String, Object> io = new LinkedHashMap<>();
        io.put("inputFile", input.toString());
        io.put("outDir", runDir.toString());
        io.put("cacheDir", cacheRoot.toString());
        json.put("io", io);

        List<Map<String, Object>> stops = new ArrayList<>();
        for (int i = 0; i < deduped.size(); i++) {
            Map<String, Object> stop = new LinkedHashMap<>();
            stop.put("id", i + 1);
            stop.put("address", deduped.get(i));
            // placeholders for Milestone 2+:
            stop.put("lat", null);
            stop.put("lng", null);
            stops.add(stop);
        }
        json.put("stops", stops);

        // placeholder routes array
        List<Map<String, Object>> routes = new ArrayList<>();
        for (int r = 1; r <= 4; r++) {
            Map<String, Object> route = new LinkedHashMap<>();
            route.put("routeIndex", r);
            route.put("orderedStopIds", Collections.emptyList());
            route.put("googleMapsUrlPrimary", null);
            route.put("googleMapsUrlFallback", Collections.emptyList());
            routes.add(route);
        }
        json.put("routes", routes);

        Path routesJson = runDir.resolve("routes.json");
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(routesJson.toFile(), json);

        // Write debug_report.txt
        Path debug = runDir.resolve("debug_report.txt");
        try (BufferedWriter w = Files.newBufferedWriter(debug, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            w.write("RoutePlanner - Debug Report (Milestone 1)\n");
            w.write("Run: " + stamp + "\n\n");
            w.write("Input file: " + input.toAbsolutePath() + "\n");
            w.write("Depot: " + depot + "\n");
            w.write("Out dir: " + runDir.toAbsolutePath() + "\n");
            w.write("Cache dir: " + cacheRoot.toAbsolutePath() + "\n\n");
            w.write("Stats:\n");
            w.write("  Raw lines:           " + rawCount + "\n");
            w.write("  Blank lines removed: " + blankRemoved + "\n");
            w.write("  After cleanup:       " + beforeDedupe + "\n");
            w.write("  Duplicates removed:  " + duplicatesRemoved + "\n");
            w.write("  Final stops:         " + deduped.size() + "\n");
        }

        System.out.println("Milestone 1 complete.");
        System.out.println("Output folder: " + runDir.toAbsolutePath());
        return 0;
    }

    private void validateRequired() {
        if (depot == null || depot.isBlank()) {
            throw new IllegalStateException("Depot is required. Set depot in run-config.json.");
        }
        if (input == null) {
            throw new IllegalStateException("Input is required. Set input in run-config.json.");
        }
        if (!Files.exists(input)) {
            throw new IllegalStateException("Input file not found: " + input.toAbsolutePath());
        }
    }
}

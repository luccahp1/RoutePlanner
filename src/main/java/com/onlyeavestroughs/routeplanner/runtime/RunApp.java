package com.onlyeavestroughs.routeplanner.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.onlyeavestroughs.routeplanner.io.AddressReader;
import com.onlyeavestroughs.routeplanner.io.ReportWriter;
import com.onlyeavestroughs.routeplanner.ors.OrsGeocoder;
import com.onlyeavestroughs.routeplanner.ors.OrsGeocoder.GeocodeOutcome;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class RunApp {
    private RunApp() {}

    public static int runFromProjectConfig() {
        try {
            ProjectConfig pcfg = ProjectConfigLoader.loadFromProjectRoot();

            String runId = makeRunId();
            RunConfig cfg = new RunConfig(
                    pcfg.depot.trim(),
                    Path.of(pcfg.input).toAbsolutePath(),
                    Path.of(pcfg.outRoot).toAbsolutePath(),
                    Path.of(pcfg.cacheRoot).toAbsolutePath(),
                    runId,
                    pcfg.orsApiKey.trim(),
                    pcfg.profile.trim()
            );

            RunDirs dirs = initDirs(cfg);

            AddressReader.ReadResult read = AddressReader.read(cfg.inputFile());
            List<String> stopsRaw = stripDepotIfPresent(read.addresses(), cfg.depotAddress());

            OrsGeocoder geocoder = new OrsGeocoder(cfg.orsApiKey(), dirs.cacheDir().resolve("geocode"));

            GeocodeOutcome depotGeo = geocoder.forwardGeocode(cfg.depotAddress());
            if (!depotGeo.success()) {
                ReportWriter.writeDebugReport(dirs.debugReport(), cfg, read, depotGeo, List.of(), List.of(), 0, 0);
                throw new IllegalStateException("Depot geocoding failed: " + depotGeo.message());
            }

            List<Stop> stops = new ArrayList<>();
            List<String> failedStops = new ArrayList<>();
            int cacheHits = depotGeo.fromCache() ? 1 : 0;
            int apiHits = depotGeo.fromCache() ? 0 : 1;

            int id = 1;
            for (String addr : stopsRaw) {
                GeocodeOutcome geo = geocoder.forwardGeocode(addr);
                if (geo.success()) {
                    stops.add(new Stop(id++, addr, geo.lat(), geo.lng()));
                    if (geo.fromCache()) cacheHits++; else apiHits++;
                } else {
                    failedStops.add(addr + " | " + geo.message());
                }
            }

            ReportWriter.writeRoutesTxt(dirs.routesTxt(), cfg, depotGeo, stops);
            writeRoutesJson(dirs.routesJson(), cfg, depotGeo, stops, failedStops);
            ReportWriter.writeDebugReport(dirs.debugReport(), cfg, read, depotGeo, stopsRaw, failedStops, cacheHits, apiHits);

            System.out.println("Run created: " + dirs.runDir());
            System.out.println("- " + dirs.routesTxt());
            System.out.println("- " + dirs.routesJson());
            System.out.println("- " + dirs.debugReport());
            return 0;

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            return 2;
        }
    }

    private static String makeRunId() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Toronto"));
        return now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    private static RunDirs initDirs(RunConfig cfg) throws Exception {
        Files.createDirectories(cfg.outRoot());
        Files.createDirectories(cfg.cacheRoot());

        Path runDir = cfg.outRoot().resolve(cfg.runId());
        Files.createDirectories(runDir);

        Path routesTxt = runDir.resolve("routes.txt");
        Path routesJson = runDir.resolve("routes.json");
        Path debug = runDir.resolve("debug_report.txt");

        Path cacheDir = cfg.cacheRoot();
        Files.createDirectories(cacheDir.resolve("geocode"));

        return new RunDirs(runDir, routesTxt, routesJson, debug, cacheDir);
    }

    private static List<String> stripDepotIfPresent(List<String> addresses, String depot) {
        String depotNorm = normalize(depot);
        List<String> out = new ArrayList<>();
        for (String a : addresses) {
            if (normalize(a).equals(depotNorm)) continue;
            out.add(a);
        }
        return out;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private static void writeRoutesJson(Path file, RunConfig cfg, GeocodeOutcome depot, List<Stop> stops, List<String> failedStops) throws Exception {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("milestone", 2);
        root.put("version", "0.2.0");
        root.put("runId", cfg.runId());
        root.put("profile", cfg.profile());

        Map<String, Object> depotObj = new LinkedHashMap<>();
        depotObj.put("address", cfg.depotAddress());
        depotObj.put("lat", depot.lat());
        depotObj.put("lng", depot.lng());
        root.put("depot", depotObj);

        Map<String, Object> io = new LinkedHashMap<>();
        io.put("inputFile", cfg.inputFile().toString());
        io.put("outDir", file.getParent().toString());
        io.put("cacheDir", cfg.cacheRoot().toString());
        root.put("io", io);

        List<Map<String, Object>> stopObjs = new ArrayList<>();
        for (Stop s : stops) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", s.id());
            o.put("address", s.address());
            o.put("lat", s.lat());
            o.put("lng", s.lng());
            stopObjs.add(o);
        }
        root.put("stops", stopObjs);
        root.put("failedStops", failedStops);

        // Placeholder for upcoming milestones (splitting, ordering, Google Maps URLs)
        root.put("routes", List.of(
                Map.of("routeIndex", 1, "orderedStopIds", List.of(), "googleMapsUrlPrimary", null, "googleMapsUrlFallback", List.of()),
                Map.of("routeIndex", 2, "orderedStopIds", List.of(), "googleMapsUrlPrimary", null, "googleMapsUrlFallback", List.of()),
                Map.of("routeIndex", 3, "orderedStopIds", List.of(), "googleMapsUrlPrimary", null, "googleMapsUrlFallback", List.of()),
                Map.of("routeIndex", 4, "orderedStopIds", List.of(), "googleMapsUrlPrimary", null, "googleMapsUrlFallback", List.of())
        ));

        mapper.writeValue(file.toFile(), root);
    }

    public record Stop(int id, String address, double lat, double lng) {}
}

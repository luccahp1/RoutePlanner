package com.onlyeavestroughs.routeplanner.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.onlyeavestroughs.routeplanner.io.AddressReader;
import com.onlyeavestroughs.routeplanner.io.ReportWriter;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "routeplanner",
        mixinStandardHelpOptions = true,
        version = "routeplanner 0.1.0",
        description = "Milestone 1: read addresses, create run folder, write initial output files."
)
public class RunCommand implements Callable<Integer> {

    @CommandLine.Option(
            names = "--depot",
            required = true,
            description = "Depot/start address for this run (driver's home)."
    )
    private String depot;

    @CommandLine.Option(
            names = {"-i", "--input"},
            required = true,
            description = "Path to addresses TXT (one address per line)."
    )
    private Path input;

    @CommandLine.Option(
            names = {"-o", "--out"},
            defaultValue = "output",
            description = "Output root directory (default: ${DEFAULT-VALUE})."
    )
    private Path outRoot;

    @CommandLine.Option(
            names = "--cache",
            defaultValue = "cache",
            description = "Cache root directory (default: ${DEFAULT-VALUE})."
    )
    private Path cacheRoot;

    @Override
    public Integer call() {
        try {
            validateInputs();

            String runId = makeRunId();
            RunConfig cfg = new RunConfig(
                    depot.trim(),
                    input.toAbsolutePath(),
                    outRoot.toAbsolutePath(),
                    cacheRoot.toAbsolutePath(),
                    runId,
                    "FAKE_ORS_KEY",
                    "driving-car"
            );
            RunDirs dirs = initDirs(cfg);

            AddressReader.ReadResult readResult = AddressReader.read(cfg.inputFile());

            // routes.txt (echo only)
            ReportWriter.writeRoutesTxt(dirs.routesTxt(), cfg, readResult.addresses());

            // routes.json (skeleton)
            writeRoutesJson(dirs.routesJson(), cfg, readResult.addresses());

            // debug_report.txt
            ReportWriter.writeDebugReport(dirs.debugReport(), cfg, readResult);

            System.out.println("Run created: " + dirs.runDir());
            System.out.println("- " + dirs.routesTxt());
            System.out.println("- " + dirs.routesJson());
            System.out.println("- " + dirs.debugReport());
            return 0;
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            return 1;
        }
    }

    private void validateInputs() throws Exception {
        if (depot == null || depot.trim().isEmpty()) {
            throw new IllegalArgumentException("--depot must not be blank");
        }
        if (input == null) {
            throw new IllegalArgumentException("--input is required");
        }
        if (!Files.exists(input)) {
            throw new IllegalArgumentException("Input file not found: " + input);
        }
        if (!Files.isRegularFile(input)) {
            throw new IllegalArgumentException("Input path is not a file: " + input);
        }
    }

    private static String makeRunId() {
        // Use user's timezone.
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

        return new RunDirs(runDir, routesTxt, routesJson, debug);
    }

    private static void writeRoutesJson(Path file, RunConfig cfg, List<String> addresses) throws Exception {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("version", "0.1.0");
        root.put("runId", cfg.runId());
        root.put("depot", cfg.depotAddress());
        root.put("inputFile", cfg.inputFile().toString());
        root.put("addressCount", addresses.size());

        // Placeholder structure for upcoming milestones.
        root.put("stops", addresses);
        root.put("routes", List.of(
                Map.of("routeIndex", 1, "stops", List.of(), "googleMapsUrl", ""),
                Map.of("routeIndex", 2, "stops", List.of(), "googleMapsUrl", ""),
                Map.of("routeIndex", 3, "stops", List.of(), "googleMapsUrl", ""),
                Map.of("routeIndex", 4, "stops", List.of(), "googleMapsUrl", "")
        ));

        mapper.writeValue(file.toFile(), root);
    }
}

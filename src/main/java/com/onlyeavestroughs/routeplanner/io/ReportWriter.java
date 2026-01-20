package com.onlyeavestroughs.routeplanner.io;

import com.onlyeavestroughs.routeplanner.runtime.RunConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Writes milestone output files.
 */
public final class ReportWriter {
    private ReportWriter() {}

    public static void writeRoutesTxt(Path file, RunConfig cfg, List<String> addresses) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("Route Planner - Milestone 1\n");
        sb.append("Run ID: ").append(cfg.runId()).append("\n");
        sb.append("Generated: ")
                .append(ZonedDateTime.now(ZoneId.of("America/Toronto"))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")))
                .append("\n\n");

        sb.append("Depot (start/end):\n");
        sb.append("- ").append(cfg.depotAddress()).append("\n\n");

        sb.append("Stops (cleaned): ").append(addresses.size()).append("\n");
        for (int i = 0; i < addresses.size(); i++) {
            sb.append(String.format("%3d. %s\n", i + 1, addresses.get(i)));
        }

        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    public static void writeDebugReport(Path file, RunConfig cfg, AddressReader.ReadResult readResult) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("Route Planner - Debug Report\n");
        sb.append("Run ID: ").append(cfg.runId()).append("\n");
        sb.append("Input: ").append(cfg.inputFile()).append("\n\n");

        sb.append("Input stats\n");
        sb.append("- Raw lines: ").append(readResult.rawLineCount()).append("\n");
        sb.append("- Blank lines removed: ").append(readResult.blankLineCount()).append("\n");
        sb.append("- Exact duplicates removed: ").append(readResult.duplicateLineCount()).append("\n");
        sb.append("- Final stop count: ").append(readResult.addresses().size()).append("\n\n");

        sb.append("Notes\n");
        sb.append("- Milestone 1 only echoes input. Geocoding/matrix/optimization are added in later milestones.\n");

        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }
}

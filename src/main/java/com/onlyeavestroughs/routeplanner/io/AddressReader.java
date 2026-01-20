package com.onlyeavestroughs.routeplanner.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Milestone 1: basic TXT ingestion.
 * - trims whitespace
 * - drops blank lines
 * - removes exact duplicates (preserves first occurrence order)
 */
public final class AddressReader {
    private AddressReader() {}

    public static ReadResult read(Path inputFile) throws IOException {
        List<String> raw = Files.readAllLines(inputFile, StandardCharsets.UTF_8);

        int blankLines = 0;
        int duplicates = 0;

        Set<String> seen = new LinkedHashSet<>();
        List<String> cleaned = new ArrayList<>();

        for (String line : raw) {
            if (line == null) continue;
            String s = line.trim();
            if (s.isEmpty()) {
                blankLines++;
                continue;
            }
            if (!seen.add(s)) {
                duplicates++;
                continue;
            }
            cleaned.add(s);
        }

        return new ReadResult(raw.size(), blankLines, duplicates, cleaned);
    }

    public record ReadResult(
            int rawLineCount,
            int blankLineCount,
            int duplicateLineCount,
            List<String> addresses
    ) {}
}

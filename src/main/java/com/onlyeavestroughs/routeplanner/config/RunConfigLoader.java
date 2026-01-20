package com.onlyeavestroughs.routeplanner.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

public final class RunConfigLoader {

    private RunConfigLoader() {}

    public static RunConfig loadFromProjectRoot() {
        try {
            Path cfgPath = Path.of("run-config.json");
            if (!Files.exists(cfgPath)) {
                throw new IllegalStateException(
                        "Missing run-config.json in project root. Create it beside pom.xml"
                );
            }

            ObjectMapper mapper = new ObjectMapper();
            RunConfig cfg = mapper.readValue(cfgPath.toFile(), RunConfig.class);

            validate(cfg);
            return cfg;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load run-config.json: " + e.getMessage(), e);
        }
    }

    private static void validate(RunConfig cfg) {
        if (cfg == null) {
            throw new IllegalStateException("run-config.json is empty or invalid JSON.");
        }
        if (cfg.depot == null || cfg.depot.isBlank()) {
            throw new IllegalStateException("run-config.json missing required field: depot");
        }
        if (cfg.input == null || cfg.input.isBlank()) {
            throw new IllegalStateException("run-config.json missing required field: input");
        }
        if (cfg.outRoot == null || cfg.outRoot.isBlank()) {
            cfg.outRoot = "output";
        }
        if (cfg.cacheRoot == null || cfg.cacheRoot.isBlank()) {
            cfg.cacheRoot = "cache";
        }
    }
}

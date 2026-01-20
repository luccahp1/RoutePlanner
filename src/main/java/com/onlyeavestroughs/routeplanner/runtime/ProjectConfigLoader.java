package com.onlyeavestroughs.routeplanner.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ProjectConfigLoader {
    private ProjectConfigLoader() {}

    public static ProjectConfig loadFromProjectRoot() {
        try {
            Path cfgPath = Path.of("run-config.json");
            if (!Files.exists(cfgPath)) {
                throw new IllegalStateException("Missing run-config.json in project root (beside pom.xml)");
            }

            ObjectMapper mapper = new ObjectMapper();
            ProjectConfig cfg = mapper.readValue(cfgPath.toFile(), ProjectConfig.class);

            validate(cfg);
            return cfg;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load run-config.json: " + e.getMessage(), e);
        }
    }

    private static void validate(ProjectConfig cfg) {
        if (cfg == null) {
            throw new IllegalStateException("run-config.json is empty or invalid JSON");
        }
        if (cfg.depot == null || cfg.depot.isBlank()) {
            throw new IllegalStateException("run-config.json missing required field: depot");
        }
        if (cfg.input == null || cfg.input.isBlank()) {
            throw new IllegalStateException("run-config.json missing required field: input");
        }

        if (cfg.orsApiKey == null || cfg.orsApiKey.isBlank()) {
            throw new IllegalStateException("run-config.json missing required field: orsApiKey");
        }

        if (cfg.profile == null || cfg.profile.isBlank()) {
            cfg.profile = "driving-car";
        }
        if (cfg.outRoot == null || cfg.outRoot.isBlank()) {
            cfg.outRoot = "output";
        }
        if (cfg.cacheRoot == null || cfg.cacheRoot.isBlank()) {
            cfg.cacheRoot = "cache";
        }
    }
}

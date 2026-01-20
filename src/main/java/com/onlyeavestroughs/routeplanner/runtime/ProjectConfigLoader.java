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
                throw new IllegalStateException(
                        "Missing run-config.json in working directory: " + Path.of(".").toAbsolutePath()
                );
            }

            ObjectMapper mapper = new ObjectMapper();
            ProjectConfig cfg = mapper.readValue(cfgPath.toFile(), ProjectConfig.class);

            validateAndDefault(cfg);
            return cfg;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load run-config.json: " + e.getMessage(), e);
        }
    }

    private static void validateAndDefault(ProjectConfig cfg) {
        if (cfg == null) throw new IllegalStateException("run-config.json is empty or invalid JSON");

        // Required
        if (isBlank(cfg.depot)) throw new IllegalStateException("run-config.json missing required field: depot");
        if (isBlank(cfg.input)) throw new IllegalStateException("run-config.json missing required field: input");
        if (isBlank(cfg.orsApiKey)) throw new IllegalStateException("run-config.json missing required field: orsApiKey");

        // Defaults
        if (isBlank(cfg.profile)) cfg.profile = "driving-car";
        if (isBlank(cfg.outRoot)) cfg.outRoot = "output";
        if (isBlank(cfg.cacheRoot)) cfg.cacheRoot = "cache";

        // Trim
        cfg.depot = cfg.depot.trim();
        cfg.input = cfg.input.trim();
        cfg.orsApiKey = cfg.orsApiKey.trim();
        cfg.profile = cfg.profile.trim();
        cfg.outRoot = cfg.outRoot.trim();
        cfg.cacheRoot = cfg.cacheRoot.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

package com.onlyeavestroughs.routeplanner.runtime;

import java.nio.file.Path;

/**
 * Immutable run configuration (Milestone 1).
 */
public record RunConfig(
        String depotAddress,
        Path inputFile,
        Path outRoot,
        Path cacheRoot,
        String runId
) {}

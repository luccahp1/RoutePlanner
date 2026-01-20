package com.onlyeavestroughs.routeplanner.runtime;

import java.nio.file.Path;

public record RunConfig(
        String depotAddress,
        Path inputFile,
        Path outRoot,
        Path cacheRoot,
        String runId,
        String orsApiKey,
        String profile
) {}

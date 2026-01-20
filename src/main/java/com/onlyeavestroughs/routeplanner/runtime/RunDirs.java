package com.onlyeavestroughs.routeplanner.runtime;

import java.nio.file.Path;

public record RunDirs(
        Path runDir,
        Path routesTxt,
        Path routesJson,
        Path debugReport,
        Path cacheDir
) {}

package com.onlyeavestroughs.routeplanner.runtime;

import java.nio.file.Path;

/**
 * Output file locations for a single run.
 */
public record RunDirs(
        Path runDir,
        Path routesTxt,
        Path routesJson,
        Path debugReport
) {}

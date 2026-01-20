package com.onlyeavestroughs.routeplanner.runtime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Configuration stored in run-config.json (project root).
 * Designed for IntelliJ ▶ runs (no CLI args required).
 *
 * NOTE: orsApiKey is read ONLY from run-config.json (no env vars).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ProjectConfig {

    public String depot;
    public String input;

    public String outRoot = "output";
    public String cacheRoot = "cache";

    /** REQUIRED: OpenRouteService API key (fake keys are fine for repo). */
    public String orsApiKey;

    /** ORS profile (we’ll keep driving-car for now). */
    public String profile = "driving-car";
}

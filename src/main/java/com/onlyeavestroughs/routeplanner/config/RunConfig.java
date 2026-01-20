package com.onlyeavestroughs.routeplanner.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RunConfig {
    public String depot;
    public String input;

    public String outRoot = "output";
    public String cacheRoot = "cache";
}

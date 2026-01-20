package com.onlyeavestroughs.routeplanner.app;

import com.onlyeavestroughs.routeplanner.cli.RoutePlannerCommand;
import com.onlyeavestroughs.routeplanner.config.RunConfig;
import com.onlyeavestroughs.routeplanner.config.RunConfigLoader;

public final class RoutePlannerApp {

    private RoutePlannerApp() {}

    public static int runFromProjectConfig() {
        try {
            RunConfig cfg = RunConfigLoader.loadFromProjectRoot();

            RoutePlannerCommand cmd = new RoutePlannerCommand();
            cmd.setDepot(cfg.depot);
            cmd.setInputPath(cfg.input);
            cmd.setOutRoot(cfg.outRoot);
            cmd.setCacheRoot(cfg.cacheRoot);

            // Milestone 1 execution (same pipeline as before, but no CLI args)
            return cmd.runMilestone1();

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            return 2;
        }
    }
}

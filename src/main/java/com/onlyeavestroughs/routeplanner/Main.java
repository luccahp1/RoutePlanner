package com.onlyeavestroughs.routeplanner;

import com.onlyeavestroughs.routeplanner.app.RoutePlannerApp;

public class Main {

    public static void main(String[] args) {
        // Milestone 1 refactor: ignore CLI args entirely.
        // Press ▶ in IntelliJ and it runs using run-config.json.
        int exit = RoutePlannerApp.runFromProjectConfig();
        System.exit(exit);
    }
}

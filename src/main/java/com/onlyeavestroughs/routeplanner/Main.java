package com.onlyeavestroughs.routeplanner;

import com.onlyeavestroughs.routeplanner.runtime.RunApp;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        int exitCode = RunApp.runFromProjectConfig();
        System.exit(exitCode);
    }
}

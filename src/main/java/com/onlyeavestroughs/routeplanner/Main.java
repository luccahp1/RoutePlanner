package com.onlyeavestroughs.routeplanner;

import com.onlyeavestroughs.routeplanner.runtime.RunApp;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        System.exit(RunApp.runFromProjectConfig());
    }
}

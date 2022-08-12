package com.github.data.utils;

import java.lang.management.ManagementFactory;

public class JvmPid {
    private static final long PID;

    public static long getPid() {
        return PID;
    }

    static {
        PID = initializePid();
    }

    private static long initializePid() {
        final String name = ManagementFactory.getRuntimeMXBean().getName();
        try {
            return Long.parseLong(name.split("@")[0]);
        } catch (final NumberFormatException e) {
            System.err.println("Failed parsing PID from [{" + name + "}]");
            return -1;
        }
    }
}
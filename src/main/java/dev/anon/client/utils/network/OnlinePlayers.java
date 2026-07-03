/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.network;

public class OnlinePlayers {
    private static long lastPingTime;

    private OnlinePlayers() {
    }

    public static void update() {
        long time = System.currentTimeMillis();

        if (time - lastPingTime > 5 * 60 * 1000) {
            AnonExecutor.execute(() -> Http.post("https://anonclient.com/api/online/ping").ignoreExceptions().send());

            lastPingTime = time;
        }
    }

    public static void leave() {
        AnonExecutor.execute(() -> Http.post("https://anonclient.com/api/online/leave").ignoreExceptions().send());
    }
}

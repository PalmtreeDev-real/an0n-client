/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.anon;

import dev.anon.client.events.Cancellable;

public class MouseScrollEvent extends Cancellable {
    private static final MouseScrollEvent INSTANCE = new MouseScrollEvent();

    public double value;

    public static MouseScrollEvent get(double value) {
        INSTANCE.setCancelled(false);
        INSTANCE.value = value;
        return INSTANCE;
    }
}

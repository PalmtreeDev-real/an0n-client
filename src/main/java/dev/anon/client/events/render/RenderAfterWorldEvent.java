/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.render;

public class RenderAfterWorldEvent {
    private static final RenderAfterWorldEvent INSTANCE = new RenderAfterWorldEvent();

    public static RenderAfterWorldEvent get() {
        return INSTANCE;
    }
}

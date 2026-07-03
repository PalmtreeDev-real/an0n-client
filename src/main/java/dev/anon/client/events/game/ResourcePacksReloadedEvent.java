/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.game;

public class ResourcePacksReloadedEvent {
    private static final ResourcePacksReloadedEvent INSTANCE = new ResourcePacksReloadedEvent();

    public static ResourcePacksReloadedEvent get() {
        return INSTANCE;
    }
}

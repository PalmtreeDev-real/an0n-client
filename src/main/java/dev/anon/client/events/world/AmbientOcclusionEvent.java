/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.world;

public class AmbientOcclusionEvent {
    private static final AmbientOcclusionEvent INSTANCE = new AmbientOcclusionEvent();

    public float lightLevel = -1;

    public static AmbientOcclusionEvent get() {
        INSTANCE.lightLevel = -1;
        return INSTANCE;
    }
}

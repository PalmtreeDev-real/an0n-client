/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.utils;

public interface CharFilter {
    boolean filter(String text, char c);

    default boolean filter(String text, int i) {
        return filter(text, (char) i);
    }
}

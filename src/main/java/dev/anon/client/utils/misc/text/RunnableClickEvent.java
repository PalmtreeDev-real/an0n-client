/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.misc.text;

/**
 * Allows arbitrary code execution in a click event
 */
public class RunnableClickEvent extends AnonClickEvent {
    public final Runnable runnable;

    public RunnableClickEvent(Runnable runnable) {
        super(null); // Should ensure no vanilla code is triggered, and only we handle it
        this.runnable = runnable;
    }
}

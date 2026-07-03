/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.game;

import dev.anon.client.events.Cancellable;
import net.minecraft.client.CameraType;

public class ChangePerspectiveEvent extends Cancellable {
    private static final ChangePerspectiveEvent INSTANCE = new ChangePerspectiveEvent();

    public CameraType perspective;

    public static ChangePerspectiveEvent get(CameraType perspective) {
        INSTANCE.setCancelled(false);
        INSTANCE.perspective = perspective;
        return INSTANCE;
    }
}

/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.entity.player;

import dev.anon.client.events.Cancellable;

public class DoAttackEvent extends Cancellable {
    private static final DoAttackEvent INSTANCE = new DoAttackEvent();

    public static DoAttackEvent get() {
        return INSTANCE;
    }
}

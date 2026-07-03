/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.entity.player;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;

public class InteractItemEvent {
    private static final InteractItemEvent INSTANCE = new InteractItemEvent();

    public InteractionHand hand;
    public InteractionResult toReturn;

    public static InteractItemEvent get(InteractionHand hand) {
        INSTANCE.hand = hand;
        INSTANCE.toReturn = null;

        return INSTANCE;
    }
}

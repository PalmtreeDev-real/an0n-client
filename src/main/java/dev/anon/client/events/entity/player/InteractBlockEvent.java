/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.entity.player;

import dev.anon.client.events.Cancellable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public class InteractBlockEvent extends Cancellable {
    private static final InteractBlockEvent INSTANCE = new InteractBlockEvent();

    public InteractionHand hand;
    public BlockHitResult result;

    public static InteractBlockEvent get(InteractionHand hand, BlockHitResult result) {
        INSTANCE.setCancelled(false);
        INSTANCE.hand = hand;
        INSTANCE.result = result;
        return INSTANCE;
    }
}

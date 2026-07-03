/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.entity.player;

import dev.anon.client.events.Cancellable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;

public class InteractEntityEvent extends Cancellable {
    private static final InteractEntityEvent INSTANCE = new InteractEntityEvent();

    public Entity entity;
    public InteractionHand hand;

    public static InteractEntityEvent get(Entity entity, InteractionHand hand) {
        INSTANCE.setCancelled(false);
        INSTANCE.entity = entity;
        INSTANCE.hand = hand;
        return INSTANCE;
    }
}

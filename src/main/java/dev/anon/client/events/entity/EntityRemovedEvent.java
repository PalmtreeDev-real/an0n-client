/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.entity;

import net.minecraft.world.entity.Entity;

public class EntityRemovedEvent {
    private static final EntityRemovedEvent INSTANCE = new EntityRemovedEvent();

    public Entity entity;

    public static EntityRemovedEvent get(Entity entity) {
        INSTANCE.entity = entity;
        return INSTANCE;
    }
}

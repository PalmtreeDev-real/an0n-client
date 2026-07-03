/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.entity;

import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

public class BoatMoveEvent {
    private static final BoatMoveEvent INSTANCE = new BoatMoveEvent();

    public AbstractBoat boat;

    public static BoatMoveEvent get(AbstractBoat entity) {
        INSTANCE.boat = entity;
        return INSTANCE;
    }
}

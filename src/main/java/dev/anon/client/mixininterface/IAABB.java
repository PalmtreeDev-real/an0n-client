/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixininterface;

import net.minecraft.core.BlockPos;

public interface IAABB {
    void anon$expand(double v);

    void anon$set(double x1, double y1, double z1, double x2, double y2, double z2);

    default void anon$set(BlockPos pos) {
        anon$set(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }
}

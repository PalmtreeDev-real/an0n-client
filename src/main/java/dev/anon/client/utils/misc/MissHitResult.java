/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.misc;

import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MissHitResult extends HitResult {
    public static final MissHitResult INSTANCE = new MissHitResult();

    private MissHitResult() {
        super(new Vec3(0, 0, 0));
    }

    @Override
    public Type getType() {
        return Type.MISS;
    }
}

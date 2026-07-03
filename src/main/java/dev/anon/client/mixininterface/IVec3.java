/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixininterface;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

@SuppressWarnings("UnusedReturnValue")
public interface IVec3 {
    Vec3 anon$set(double x, double y, double z);

    default Vec3 anon$set(Vec3i vec) {
        return anon$set(vec.getX(), vec.getY(), vec.getZ());
    }

    default Vec3 anon$set(Vector3d vec) {
        return anon$set(vec.x, vec.y, vec.z);
    }

    default Vec3 anon$set(Vec3 pos) {
        return anon$set(pos.x, pos.y, pos.z);
    }

    Vec3 anon$setXZ(double x, double z);

    Vec3 anon$setY(double y);
}

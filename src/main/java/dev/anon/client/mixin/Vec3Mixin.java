/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import dev.anon.client.mixininterface.IVec3;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec3.class)
public abstract class Vec3Mixin implements IVec3 {
    @Shadow
    @Final
    @Mutable
    public double x;
    @Shadow
    @Final
    @Mutable
    public double y;
    @Shadow
    @Final
    @Mutable
    public double z;

    @Override
    public Vec3 anon$set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        return (Vec3) (Object) this;
    }

    @Override
    public Vec3 anon$setXZ(double x, double z) {
        this.x = x;
        this.z = z;

        return (Vec3) (Object) this;
    }

    @Override
    public Vec3 anon$setY(double y) {
        this.y = y;

        return (Vec3) (Object) this;
    }
}

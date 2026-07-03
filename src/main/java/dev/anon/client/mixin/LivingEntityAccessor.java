/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Invoker("jumpInLiquid")
    void anon$swimUpwards(TagKey<Fluid> fluid);

    @Accessor("jumping")
    boolean anon$isJumping();

    @Accessor("noJumpDelay")
    int anon$getJumpCooldown();

    @Accessor("noJumpDelay")
    void anon$setJumpCooldown(int cooldown);
}

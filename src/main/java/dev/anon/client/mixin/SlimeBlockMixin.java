/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.movement.NoSlow;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.anon.client.AnonClient.mc;

@Mixin(SlimeBlock.class)
public abstract class SlimeBlockMixin {
    @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
    private void onStepOn(Level level, BlockPos pos, BlockState onState, Entity entity, CallbackInfo ci) {
        if (Modules.get().get(NoSlow.class).slimeBlock() && entity == mc.player) ci.cancel();
    }
}

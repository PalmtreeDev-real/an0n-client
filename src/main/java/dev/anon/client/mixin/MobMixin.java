/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.movement.EntityControl;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
public abstract class MobMixin {
    @ModifyReturnValue(method = "isSaddled", at = @At("RETURN"))
    private boolean isSaddled(boolean original) {
        return Modules.get().get(EntityControl.class).spoofSaddle() || original;
    }
}

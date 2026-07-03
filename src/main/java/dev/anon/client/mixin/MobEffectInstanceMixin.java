/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.player.PotionSaver;
import dev.anon.client.utils.Utils;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin {
    @Inject(method = "tickDownDuration", at = @At("HEAD"), cancellable = true)
    private void tick(CallbackInfo ci) {
        if (!Utils.canUpdate()) return;

        if (Modules.get().get(PotionSaver.class).shouldFreeze(((MobEffectInstance) (Object) this).getEffect().value())) {
            ci.cancel();
        }
    }
}

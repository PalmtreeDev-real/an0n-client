/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.render.NoRender;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {FireworkParticles.SparkParticle.class, FireworkParticles.OverlayParticle.class})
public abstract class FireworksSparkParticleSubMixin {
    @Inject(method = "extract", at = @At("HEAD"), cancellable = true)
    private void buildExplosionGeometry(QuadParticleRenderState particleTypeRenderState, Camera camera, float partialTickTime, CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noFireworkExplosions()) ci.cancel();
    }
}

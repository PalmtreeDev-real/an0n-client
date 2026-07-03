/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.render.Fullbright;
import dev.anon.client.systems.modules.render.Xray;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.profiling.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lightmap.class)
public abstract class LightmapMixin {
    @Shadow
    @Final
    private GpuTexture texture;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render$fullbright(LightmapRenderState renderState, CallbackInfo ci) {
        if (Modules.get().get(Fullbright.class).getGamma() || Modules.get().isActive(Xray.class)) {
            var profile = Profiler.get();
            profile.push("lightmap");

            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(texture, ARGB.color(255, 255, 255, 255));

            profile.pop();
            ci.cancel();
        }
    }
}

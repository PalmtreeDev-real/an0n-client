/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.render.NoRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void onRenderFireOverlay(PoseStack poseStack, MultiBufferSource bufferSource, TextureAtlasSprite sprite, CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noFireOverlay()) ci.cancel();
    }

    @Inject(method = "renderWater", at = @At("HEAD"), cancellable = true)
    private static void onRenderUnderwaterOverlay(Minecraft minecraft, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noLiquidOverlay()) ci.cancel();
    }

    @Inject(method = "renderTex", at = @At("HEAD"), cancellable = true)
    private static void render(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noInWallOverlay()) ci.cancel();
    }
}

/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ShadowFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShadowFeatureRenderer.class)
public abstract class ShadowFeatureRendererMixin {
    @Inject(method = "renderTranslucent", at = @At("HEAD"), cancellable = true)
    private void anon$onRenderTranslucent(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, CallbackInfo ci) {
        if (nodeCollection.getShadowSubmits().isEmpty()) {
            ci.cancel();
        }
    }
}

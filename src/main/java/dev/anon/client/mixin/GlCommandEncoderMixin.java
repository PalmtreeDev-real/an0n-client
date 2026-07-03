/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPassBackend;
import dev.anon.client.mixininterface.IGpuDevice;
import dev.anon.client.mixininterface.IRenderPipeline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.lwjgl.opengl.GL11C.*;

@Mixin(GlCommandEncoder.class)
public abstract class GlCommandEncoderMixin {
    @Shadow
    @Final
    private GlDevice device;

    @SuppressWarnings("deprecation")
    @Inject(method = "createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalInt;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/blaze3d/systems/RenderPassBackend;", at = @At("RETURN"))
    private void createRenderPass$iGpuDevice(CallbackInfoReturnable<RenderPassBackend> cir) {
        ((IGpuDevice) device).anon$onCreateRenderPass(cir.getReturnValue());
    }

    @Inject(method = "applyPipelineState", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_polygonMode(II)V"))
    private void setPipelineAndApplyState$lineSmooth(RenderPipeline pipeline, CallbackInfo ci) {
        if (((IRenderPipeline) pipeline).anon$getLineSmooth()) {
            glEnable(GL_LINE_SMOOTH);
            glLineWidth(1);
        } else {
            glDisable(GL_LINE_SMOOTH);
        }
    }
}

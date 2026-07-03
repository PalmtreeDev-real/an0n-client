/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.anon.client.renderer.MeshUniforms;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.misc.InventoryTweaks;
import dev.anon.client.utils.render.postprocess.ChamsShader;
import dev.anon.client.utils.render.postprocess.OutlineUniforms;
import dev.anon.client.utils.render.postprocess.PostProcessShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.anon.client.AnonClient.mc;

@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {
    @Inject(method = "flipFrame", at = @At("TAIL"))
    private static void anon$flipFrame(CallbackInfo ci) {
        MeshUniforms.flipFrame();
        PostProcessShader.flipFrame();
        ChamsShader.flipFrame();
        OutlineUniforms.flipFrame();

        if (Modules.get() == null || mc.player == null) return;
        if (Modules.get().get(InventoryTweaks.class).frameInput()) ((MinecraftAccessor) mc).anon$handleInputEvents();
    }
}

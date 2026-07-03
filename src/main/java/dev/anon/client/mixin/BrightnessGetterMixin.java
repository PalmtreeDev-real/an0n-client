/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.render.Fullbright;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LevelRenderer.BrightnessGetter.class)
public interface BrightnessGetterMixin {
    @ModifyVariable(method = "lambda$static$0", at = @At(value = "STORE"), name = "sky")
    private static int getLightmapCoordinatesModifySkyLight(int sky) {
        return Math.max(Modules.get().get(Fullbright.class).getLuminance(LightLayer.SKY), sky);
    }

    @ModifyVariable(method = "lambda$static$0", at = @At(value = "STORE"), name = "block")
    private static int getLightmapCoordinatesModifyBlockLight(int block) {
        return Math.max(Modules.get().get(Fullbright.class).getLuminance(LightLayer.BLOCK), block);
    }
}

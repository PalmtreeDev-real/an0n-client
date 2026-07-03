/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.misc.AntiPacketKick;
import net.minecraft.network.CompressionDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CompressionDecoder.class)
public abstract class CompressionDecoderMixin {
    @ModifyExpressionValue(method = "decode", at = @At(value = "CONSTANT", args = "intValue=8388608"))
    private int anon$maximizeUncompressedPacketLimit(int original) {
        return Modules.get().isActive(AntiPacketKick.class) ? Integer.MAX_VALUE : original;
    }
}

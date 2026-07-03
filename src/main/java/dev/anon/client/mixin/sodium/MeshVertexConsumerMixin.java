/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin.sodium;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.anon.client.utils.render.MeshBuilderVertexConsumerProvider;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MeshBuilderVertexConsumerProvider.MeshBuilderVertexConsumer.class, remap = false)
public abstract class MeshVertexConsumerMixin implements VertexConsumer, VertexBufferWriter {
    @Override
    public void push(MemoryStack stack, long ptr, int count, VertexFormat format) {
        int positionOffset = format.getOffset(VertexFormatElement.POSITION);

        if (positionOffset == -1) return;

        for (int i = 0; i < count; i++) {
            long positionPtr = ptr + (long) format.getVertexSize() * i + positionOffset;

            float x = MemoryUtil.memGetFloat(positionPtr);
            float y = MemoryUtil.memGetFloat(positionPtr + 4);
            float z = MemoryUtil.memGetFloat(positionPtr + 8);

            addVertex(x, y, z);
        }
    }
}

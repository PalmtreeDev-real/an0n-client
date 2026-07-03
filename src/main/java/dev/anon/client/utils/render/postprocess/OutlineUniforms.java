/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.render.postprocess;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.renderer.DynamicUniformStorage;

import java.nio.ByteBuffer;

public class OutlineUniforms {
    private static final int UNIFORM_SIZE = new Std140SizeCalculator()
        .putInt()
        .putFloat()
        .putInt()
        .putFloat()
        .get();

    private static final DynamicUniformStorage<Data> STORAGE = new DynamicUniformStorage<>("AN0N - Outline UBO", UNIFORM_SIZE, 16);

    public static void flipFrame() {
        STORAGE.endFrame();
    }

    public static GpuBufferSlice write(int width, float fillOpacity, int shapeMode, float glowMultiplier) {
        return STORAGE.writeUniform(new Data(width, fillOpacity, shapeMode, glowMultiplier));
    }

    private record Data(int width, float fillOpacity, int shapeMode,
                        float glowMultiplier) implements DynamicUniformStorage.DynamicUniform {
        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                .putInt(width)
                .putFloat(fillOpacity)
                .putInt(shapeMode)
                .putFloat(glowMultiplier);
        }
    }
}

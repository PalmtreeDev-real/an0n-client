/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.function.Supplier;

public class WrapperImmediateVertexConsumerProvider extends MultiBufferSource.BufferSource {
    private final Supplier<MultiBufferSource> supplier;

    public WrapperImmediateVertexConsumerProvider(Supplier<MultiBufferSource> supplier) {
        super(null, null);
        this.supplier = supplier;
    }

    @Override
    public VertexConsumer getBuffer(RenderType layer) {
        return supplier.get().getBuffer(layer);
    }

    @Override
    public void endBatch() {
    }

    @Override
    public void endBatch(RenderType layer) {
    }
}

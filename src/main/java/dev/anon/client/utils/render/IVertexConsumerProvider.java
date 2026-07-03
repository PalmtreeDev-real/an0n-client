/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.render;

import net.minecraft.client.renderer.MultiBufferSource;

public interface IVertexConsumerProvider extends MultiBufferSource {
    void setOffset(int offsetX, int offsetY, int offsetZ);
}

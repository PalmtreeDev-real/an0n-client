/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixininterface;

import com.mojang.blaze3d.pipeline.RenderTarget;

public interface ILevelRenderer {
    void anon$pushEntityOutlineFramebuffer(RenderTarget framebuffer);

    void anon$popEntityOutlineFramebuffer();
}

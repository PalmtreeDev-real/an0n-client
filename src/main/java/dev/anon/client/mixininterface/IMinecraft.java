/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixininterface;

import com.mojang.blaze3d.pipeline.RenderTarget;

public interface IMinecraft {
    void anon$rightClick();

    void anon$setFramebuffer(RenderTarget framebuffer);
}

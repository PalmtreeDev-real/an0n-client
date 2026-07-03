/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixininterface;

import com.mojang.blaze3d.systems.RenderPassBackend;

public interface IGpuDevice {
    /**
     * Currently there can only be a single scissor pushed at once.
     */
    void anon$pushScissor(int x, int y, int width, int height);

    void anon$popScissor();

    /**
     * This is an *INTERNAL* method, it shouldn't be called.
     */
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    void anon$onCreateRenderPass(RenderPassBackend backend);
}

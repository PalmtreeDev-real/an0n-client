/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.systems.RenderPassBackend;
import dev.anon.client.mixininterface.IGpuDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GlDevice.class)
public abstract class GlDeviceMixin implements IGpuDevice {
    @Unique
    private int x, y, width, height;

    @Unique
    private boolean set;

    @Override
    public void anon$pushScissor(int x, int y, int width, int height) {
        if (set)
            throw new IllegalStateException("Currently there can only be one global scissor pushed");

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        set = true;
    }

    @Override
    public void anon$popScissor() {
        if (!set)
            throw new IllegalStateException("No scissor pushed");

        set = false;
    }

    @Deprecated
    @Override
    public void anon$onCreateRenderPass(RenderPassBackend backend) {
        if (set) {
            backend.enableScissor(x, y, width, height);
        }
    }
}

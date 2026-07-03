/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.systems.RenderPassBackend;
import dev.anon.client.mixininterface.IGpuDevice;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GpuDevice.class)
public abstract class GpuDeviceMixin implements IGpuDevice {
    @Shadow
    @Final
    private GpuDeviceBackend backend;

    @Override
    public void anon$pushScissor(int x, int y, int width, int height) {
        ((IGpuDevice) backend).anon$pushScissor(x, y, width, height);
    }

    @Override
    public void anon$popScissor() {
        ((IGpuDevice) backend).anon$popScissor();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void anon$onCreateRenderPass(RenderPassBackend backend) {
        ((IGpuDevice) this.backend).anon$onCreateRenderPass(backend);
    }
}

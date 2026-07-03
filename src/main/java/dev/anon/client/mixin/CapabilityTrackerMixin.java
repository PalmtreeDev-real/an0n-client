/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.mojang.blaze3d.opengl.GlStateManager;
import dev.anon.client.mixininterface.ICapabilityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GlStateManager.BooleanState.class)
public abstract class CapabilityTrackerMixin implements ICapabilityTracker {
    @Shadow
    private boolean enabled;

    @Shadow
    public abstract void setEnabled(boolean enabled);

    @Override
    public boolean anon$get() {
        return enabled;
    }

    @Override
    public void anon$set(boolean state) {
        setEnabled(state);
    }
}

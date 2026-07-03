/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import dev.anon.client.mixininterface.IComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MutableComponent.class)
public abstract class MutableComponentMixin implements IComponent {
    @Shadow
    private @Nullable Language decomposedWith;

    @Override
    public void anon$invalidateCache() {
        this.decomposedWith = null;
    }
}

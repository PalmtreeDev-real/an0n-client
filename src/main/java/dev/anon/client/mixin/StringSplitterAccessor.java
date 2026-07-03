/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import net.minecraft.client.StringSplitter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StringSplitter.class)
public interface StringSplitterAccessor {
    @Accessor("widthProvider")
    StringSplitter.WidthProvider anon$getWidthProvider();
}

/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.utils;

import dev.anon.client.utils.misc.ISerializable;
import net.minecraft.nbt.CompoundTag;

public class WindowConfig implements ISerializable<WindowConfig> {
    public boolean expanded = true;
    public double x = -1;
    public double y = -1;

    // Saving

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putBoolean("expanded", expanded);
        tag.putDouble("x", x);
        tag.putDouble("y", y);

        return tag;
    }

    @Override
    public WindowConfig fromTag(CompoundTag tag) {
        tag.getBoolean("expanded").ifPresent(bool -> expanded = bool);
        tag.getDouble("x").ifPresent(x1 -> x = x1);
        tag.getDouble("y").ifPresent(y1 -> y = y1);

        return this;
    }
}

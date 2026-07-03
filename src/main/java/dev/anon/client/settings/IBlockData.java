/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.settings;

import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.WidgetScreen;
import dev.anon.client.utils.misc.IChangeable;
import dev.anon.client.utils.misc.ICopyable;
import dev.anon.client.utils.misc.ISerializable;
import net.minecraft.world.level.block.Block;

public interface IBlockData<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> {
    WidgetScreen createScreen(GuiTheme theme, Block block, BlockDataSetting<T> setting);
}

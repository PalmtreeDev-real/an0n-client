/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.settings;

import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.WidgetScreen;
import dev.anon.client.utils.misc.ICopyable;
import dev.anon.client.utils.misc.ISerializable;

public interface IGeneric<T extends IGeneric<T>> extends ICopyable<T>, ISerializable<T> {
    WidgetScreen createScreen(GuiTheme theme, GenericSetting<T> setting);
}

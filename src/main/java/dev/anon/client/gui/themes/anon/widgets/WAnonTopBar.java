/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.themes.anon.widgets;

import dev.anon.client.gui.themes.anon.AnonWidget;
import dev.anon.client.gui.widgets.WTopBar;
import dev.anon.client.utils.render.color.Color;

public class WAnonTopBar extends WTopBar implements AnonWidget {
    @Override
    protected Color getButtonColor(boolean pressed, boolean hovered) {
        return theme().backgroundColor.get(pressed, hovered);
    }

    @Override
    protected Color getNameColor() {
        return theme().textColor.get();
    }
}

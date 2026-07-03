/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.themes.anon.widgets.pressable;

import dev.anon.client.gui.themes.anon.AnonWidget;
import dev.anon.client.gui.widgets.pressable.WFavorite;
import dev.anon.client.utils.render.color.Color;

public class WAnonFavorite extends WFavorite implements AnonWidget {
    public WAnonFavorite(boolean checked) {
        super(checked);
    }

    @Override
    protected Color getColor() {
        return theme().favoriteColor.get();
    }
}

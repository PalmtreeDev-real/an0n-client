/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.widgets.pressable;

public abstract class WTriangle extends WPressable {
    public double rotation;

    @Override
    protected void onCalculateSize() {
        double s = theme.textHeight();

        width = s;
        height = s;
    }
}

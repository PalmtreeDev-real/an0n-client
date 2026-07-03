/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.themes.anon.widgets.pressable;

import dev.anon.client.gui.renderer.GuiRenderer;
import dev.anon.client.gui.themes.anon.AnonGuiTheme;
import dev.anon.client.gui.themes.anon.AnonWidget;
import dev.anon.client.gui.widgets.pressable.WConfirmedMinus;
import dev.anon.client.utils.render.color.Color;

public class WAnonConfirmedMinus extends WConfirmedMinus implements AnonWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        AnonGuiTheme theme = theme();
        double pad = pad();
        double s = theme.scale(3);

        Color outline = theme.outlineColor.get(pressed, mouseOver);
        Color fg = pressedOnce ? theme.backgroundColor.get(pressed, mouseOver) : theme().minusColor.get();
        Color bg = pressedOnce ? theme().minusColor.get() : theme.backgroundColor.get(pressed, mouseOver);

        renderBackground(renderer, this, outline, bg);
        renderer.quad(x + pad, y + height / 2 - s / 2, width - pad * 2, s, fg);
    }
}

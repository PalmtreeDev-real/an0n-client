/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.themes.anon.widgets.pressable;

import dev.anon.client.gui.renderer.GuiRenderer;
import dev.anon.client.gui.themes.anon.AnonGuiTheme;
import dev.anon.client.gui.themes.anon.AnonWidget;
import dev.anon.client.gui.widgets.pressable.WPlus;

public class WAnonPlus extends WPlus implements AnonWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        AnonGuiTheme theme = theme();
        double pad = pad();
        double s = theme.scale(3);

        renderBackground(renderer, this, pressed, mouseOver);
        renderer.quad(x + pad, y + height / 2 - s / 2, width - pad * 2, s, theme.plusColor.get());
        renderer.quad(x + width / 2 - s / 2, y + pad, s, height - pad * 2, theme.plusColor.get());
    }
}

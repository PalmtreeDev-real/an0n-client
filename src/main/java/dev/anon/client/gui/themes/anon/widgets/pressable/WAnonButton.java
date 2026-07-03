/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.themes.anon.widgets.pressable;

import dev.anon.client.gui.renderer.GuiRenderer;
import dev.anon.client.gui.renderer.packer.GuiTexture;
import dev.anon.client.gui.themes.anon.AnonGuiTheme;
import dev.anon.client.gui.themes.anon.AnonWidget;
import dev.anon.client.gui.widgets.pressable.WButton;

public class WAnonButton extends WButton implements AnonWidget {
    public WAnonButton(String text, GuiTexture texture) {
        super(text, texture);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        AnonGuiTheme theme = theme();
        double pad = pad();

        renderBackground(renderer, this, pressed, mouseOver);

        if (text != null) {
            renderer.text(text, x + width / 2 - textWidth / 2, y + pad, theme.textColor.get(), false);
        }
        else {
            double ts = theme.textHeight();
            renderer.quad(x + width / 2 - ts / 2, y + pad, ts, ts, texture, theme.textColor.get());
        }
    }
}

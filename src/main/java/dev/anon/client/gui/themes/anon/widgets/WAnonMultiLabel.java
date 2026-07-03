/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.themes.anon.widgets;

import dev.anon.client.gui.renderer.GuiRenderer;
import dev.anon.client.gui.themes.anon.AnonWidget;
import dev.anon.client.gui.widgets.WMultiLabel;
import dev.anon.client.utils.render.color.Color;

public class WAnonMultiLabel extends WMultiLabel implements AnonWidget {
    public WAnonMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title, maxWidth);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double h = theme.textHeight(title);
        Color defaultColor = theme().textColor.get();

        for (int i = 0; i < lines.size(); i++) {
            renderer.text(lines.get(i), x, y + h * i, color != null ? color : defaultColor, false);
        }
    }
}

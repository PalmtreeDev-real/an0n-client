/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.themes.anon.widgets;

import dev.anon.client.gui.renderer.GuiRenderer;
import dev.anon.client.gui.themes.anon.AnonWidget;
import dev.anon.client.gui.widgets.containers.WView;

public class WAnonView extends WView implements AnonWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (canScroll && hasScrollBar) {
            renderer.quad(handleX(), handleY(), handleWidth(), handleHeight(), theme().scrollbarColor.get(focused, handleMouseOver));
        }
    }
}

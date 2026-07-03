/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.themes.anon.widgets;

import dev.anon.client.gui.renderer.GuiRenderer;
import dev.anon.client.gui.themes.anon.AnonWidget;
import dev.anon.client.gui.widgets.WWidget;
import dev.anon.client.gui.widgets.containers.WSection;
import dev.anon.client.gui.widgets.pressable.WTriangle;

public class WAnonSection extends WSection {
    public WAnonSection(String title, boolean expanded, WWidget headerWidget) {
        super(title, expanded, headerWidget);
    }

    @Override
    protected WHeader createHeader() {
        return new WAnonHeader(title);
    }

    protected class WAnonHeader extends WHeader {
        private WTriangle triangle;

        public WAnonHeader(String title) {
            super(title);
        }

        @Override
        public void init() {
            add(theme.horizontalSeparator(title)).expandX();

            if (headerWidget != null) add(headerWidget);

            triangle = new WHeaderTriangle();
            triangle.theme = theme;
            triangle.action = this::onClick;

            add(triangle);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            triangle.rotation = (1 - animProgress) * -90;
        }
    }

    protected static class WHeaderTriangle extends WTriangle implements AnonWidget {
        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.rotatedQuad(x, y, width, height, rotation, GuiRenderer.TRIANGLE, theme().textColor.get());
        }
    }
}
